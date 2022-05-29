import parametrages    #on importe le fichier des variables necessaire au bon fonctionnement du programme

import socket

import requests    #permet l'envoi de requêtes HTTP
import json
import geohash

import paho.mqtt.client as paho

import board    #chaque port a un board répertoire (adresse) contenant des cartes appartenant à une ligne de microcontrôleur spécifique

import sys      #interagit avec l’interpréteur Python
import time

from ntpdatetime import now    # permet le renvoi de l'heure actuelle qui est récupérée à partir d'un serveur NTP

import adafruit_shtc3    #adresse du capteur de température et d'humidité
import adafruit_lps2x    #adresse du capteur de pression

#i2c: bus informatique (capteurs sensehat(B)) --> ligne horloge et données utilisées à la fois pour envoi et réception données 

i2c = board.I2C()

def recup_capteurs():    #renvoie température, humidité, pression
    sht = adafruit_shtc3.SHTC3(i2c)    #initialisation de la variable du capteur de température/humidité
    lps = adafruit_lps2x.LPS22(i2c,0x5c)    #initialisation de la variable du capteur de pression, changement d'adresse 0x5c
    return sht.temperature, sht.relative_humidity, lps.pressure

########################################################## ENVOI DONNEES CAPTEURS #####################################################################

def on_connect(client, userdata, flags, rc):    #void
    if rc==0:
        print("Connecté")
    else:
        print("Mauvaise connection")

def on_disconnect(client, userdata, rc):    #void
    if rc==0:
        print("\nDéconnecté")

def connection_MQ(lien_broker,port):    #(str,int)->void
    client.on_connect = on_connect
    client.connect(lien_broker, port)
    client.loop_start()

def recup_heure():   #void->str
    temps, recupere = now()
    while recupere!= True :     #vérifie si la date a été récupérée
        temps, recupere = now()
    date = temps.strftime("%s%f")[:-3]    #convertit la date en chaine de caractères et la formate en millisecondes
    return date

def envoie_donnee(topic,valeur):   #(str,valeur)->void
    date_envoi = recup_heure()
    msg_publish = date_envoi + " " + str(valeur)
    result = client.publish(topic, msg_publish)

def localisation():    #void->Str
    ip_publique = requests.get('https://api.ipify.org')    #on obtient l'adresse ip publique du raspberry à l'aide de ce site internet
    while (ip_publique.status_code != 200):    #vérifie l'accès au site
        print ("Erreur site : https://api.ipify.org \n")
        ip_publique = requests.get('https://api.ipify.org')

    url='http://ip-api.com/json/' + ip_publique.text    #on concatène l'ip avec le site internet suivant pour localiser le raspberry avec des coordonnées précises
    reponse=requests.get(url)    #on récupère avec la requête get les données de localisation du raspberry
    while (reponse.status_code != 200):
        print ("Erreur site : " + url + "\n")
        reponse=requests.get(url)

    data = json.loads(reponse.text)    #on convertit la chaîne json en dictionnaire python (dict{'key':value})
    long = data['lon']
    lat = data['lat']
    geolocate = geohash.encode(lat,long) # les données latitude, longitude sont converties en geohash pour grafana 
    return geolocate

def sign_out(client):    #void
    client.on_disconnect = on_disconnect
    client.loop_stop()
    client.disconnect()

#########################       Main      #########################################################################################

client = paho.Client()
connection_MQ(parametrages.url,parametrages.port)
time.sleep(1)    #attente d'une seconde avant d'entrer dans le while pour s'assurer que la connexion ait le temps de s'établir
print ("Envoi des données en cours:\n")
coordonnees = localisation()
while True:
    try:
        envoie_donnee(parametrages.topic_coordonnees,coordonnees)
        temperature, humidite, pression = recup_capteurs()
        envoie_donnee(parametrages.topic_temperature,temperature)
        envoie_donnee(parametrages.topic_humidite,humidite)
        envoie_donnee(parametrages.topic_pression,pression)
        time.sleep(parametrages.temps)
    except KeyboardInterrupt:
        sign_out(client)                     #on s'assure que le client soit déconnecté avant l'arrêt du programme
        sys.exit("\nArrêt du programme.")    #termine le programme en cours d'exécution
