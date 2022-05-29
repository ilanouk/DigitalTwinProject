import parametrages    #on importe le fichier des variables necessaire au bon fonctionnement du programme

import paho.mqtt.client as paho

import sys      #interagit avec l’interpréteur Python
import time

from ntpdatetime import now    # permet le renvoi de l'heure actuelle qui est récupérée à partir d'un serveur NTP

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

def envoie_donnee(topic,valeur):   #(str,float)->void
    date_envoi = recup_heure()
    msg_publish = date_envoi + " " + str(valeur)
    print ("Envoi du message: " + msg_publish)
    result = client.publish(topic, msg_publish)

def sign_out(client):    #void
    client.on_disconnect = on_disconnect
    client.loop_stop()
    client.disconnect()

#########################       Main      #########################################################################################

client = paho.Client()
connection_MQ(parametrages.url,parametrages.port)
time.sleep(1)    #attente d'une seconde avant d'entrer dans le while pour s'assurer que la connexion ait le temps de s'établir
print ("Test envoi des données en cours:\n")
while True:
    try:
        envoie_donnee(parametrages.topic_test,"message_test_raspberry")
        time.sleep(parametrages.temps)
    except KeyboardInterrupt:
        sign_out(client)                     #on s'assure que le client soit déconnecté avant l'arrêt du programme
        sys.exit("\nArrêt du programme.")    #termine le programme en cours d'exécution
