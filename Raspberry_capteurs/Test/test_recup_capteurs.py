import parametrages    #on importe le fichier des variables necessaire au bon fonctionnement du programme

import board    #chaque port a un board répertoire (adresse) contenant des cartes appartenant à une ligne de microcontrôleur spécifique

import sys      #interagit avec l’interpréteur Python
import time

import adafruit_shtc3    #adresse du capteur de température et d'humidité
import adafruit_lps2x    #adresse du capteur de pression

#i2c: bus informatique (capteurs sensehat(B)) --> ligne horloge et données utilisées à la fois pour envoi et réception données 

i2c = board.I2C()

def recup_capteurs():    #renvoie température, humidité, pression
    sht = adafruit_shtc3.SHTC3(i2c)    #initialisation de la variable du capteur de température/humidité
    lps = adafruit_lps2x.LPS22(i2c,0x5c)    #initialisation de la variable du capteur de pression, changement d'adresse 0x5c
    return sht.temperature, sht.relative_humidity, lps.pressure



#########################       Main      #########################################################################################

print ("Test réception données des capteurs:\n")
while True:
    try:
        temperature, humidite, pression = recup_capteurs()
        print ("Température: %0.1f °C" % temperature)
        print ("Humidité: %0.1f %%fH" % humidite)
        print ("Pression: %.2f hPa" % pression)
        time.sleep(parametrages.temps)
    except KeyboardInterrupt:
        sys.exit("\nArrêt du programme.")    #termine le programme en cours d'exécution
