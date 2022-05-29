/// source : https://www.hivemq.com/blog/mqtt-client-library-encyclopedia-eclipse-paho-java/
/// http://ashishdoneriya.github.io/influxdb-java/


import com.db.influxdb.*;



import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.Arrays;


import java.util.Properties;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit; 

import java.util.StringTokenizer;



public class RecoitCapteurs {


	public static void connecterClient(MqttClient client,String topic_temperature,String topic_humidite,String topic_pression,String topic_coordonnees) throws Exception {   //Connection au MQTT et aux differents topics
	client.connect(); //Connection au MQTT
    		
    		if (client.isConnected()) {
    			System.out.println("Connecté"); 
    			
    			// Abonnement aux différents topics
    			
    			client.subscribe(topic_temperature,1);
    			client.subscribe(topic_humidite,1);
    			client.subscribe(topic_pression,1);
    			client.subscribe(topic_coordonnees,1);
    			
    		
    		}
    		else{
    		System.out.println("Erreur de connexion");
    	
  		}

	}
	public static void ecrireDB(Configuration configuration,String topic,MqttMessage message,String topic_temperature,String 			topic_humidite,String topic_pression,String topic_coordonnees) throws Exception {    //Ecrit une valeur dans la base de données
		
		DataWriter writer = new DataWriter(configuration); 
		     
		writer.setTimeUnit(TimeUnit.MILLISECONDS);
				
		if (topic.equals(topic_temperature)){
			writer.setMeasurement("temperature"); //Choix de la table où ajouter des données
			
		}
		
		if (topic.equals(topic_humidite)){
			writer.setMeasurement("humidite");
		}
		
		if (topic.equals(topic_pression)){
			writer.setMeasurement("pression");
		}
		
		if (topic.equals(topic_coordonnees)){
			writer.setMeasurement("coordonnees");
		}

		String msg = message.toString();
		StringTokenizer msgT = new StringTokenizer(msg);
		String temps = msgT.nextToken();
		String valeur = msgT.nextToken();
		long tmp = Long.parseLong(temps); // transormation en float
		
		if (!topic.equals(topic_coordonnees)){
			float val = Float.parseFloat(valeur);
			writer.addField("value",val);
		}
		

		
		else{
		
			writer.addField("geo",valeur);
			writer.addField("metric",1);
		}
		
		
		writer.setTime(tmp);
		
		writer.writeData();
		
		writer.setMeasurement("etat");
		writer.addField("value",1.0f);
		writer.writeData();

	}

	public static String recupFichier(String fichier){  	///Recupere le chemin d'accèes a un fichier dans le meme dossier que ce script
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String configPath = rootPath + "configJava";	
		return configPath;
	}








	public static void main(String[] args) throws Exception {
	
	
	
	/// Récupération des informations du fichier de configuration
		
		Properties appProps = new Properties();
		appProps.load(new FileInputStream(recupFichier("configJava")));

		String topic_humidite = appProps.getProperty("topic_humidite");
		String topic_temperature = appProps.getProperty("topic_temperature"); 		
		String topic_pression = appProps.getProperty("topic_pression");	
		String topic_coordonnees = appProps.getProperty("topic_coordonnees");	
		String lienMqtt = appProps.getProperty("lienMqtt");
		String portMqtt = appProps.getProperty("portMqtt");
		String database = appProps.getProperty("database");
		String userInflux = appProps.getProperty("userInflux");
		String mdpInflux = appProps.getProperty("mdpInflux");
		String ipServeur = appProps.getProperty("ipServeur");
		String portInflux = appProps.getProperty("portInflux");
		
		
		//Configuration de la connexion au MQTT
		MqttClient client = new MqttClient(   
		lienMqtt+":"+portMqtt, 
    		MqttClient.generateClientId(), 
    		new MemoryPersistence()); 
    		
    		// Configuration de la connexion  à la base de donnée
    		
    		Configuration configuration = new Configuration("localhost", "8086",
	  	userInflux, mdpInflux, database); // ip, port, user, password, base de donnée
	  	
    		Utilities utilities = new Utilities();
		utilities.createDatabase(configuration);
		
		
    		
    		
    		
    		
    		
    		client.setCallback(new MqttCallback() {

    		@Override
    		public void connectionLost(Throwable cause) { //Lorsque la connexion est perdu
    		
    			System.out.println("Connexion perdue");
    		
    		}

    		@Override
    		
    		//Ecriture des données dans la base de donnée lorsqu'un message arrive
    		
    		public void messageArrived(String topic, MqttMessage message) throws Exception { //	Lorsque l'on reçoit un message
    			if (topic.equals(topic_coordonnees)){
    				utilities.dropMeasurement(configuration, "coordonnees");
    			}
	  		ecrireDB(configuration,topic,message,topic_temperature,topic_humidite,topic_pression,topic_coordonnees);
			
	
    		}
    		
    		
		@Override
    		public void deliveryComplete(IMqttDeliveryToken token) {
    		
    		}
	});
	
	connecterClient(client,topic_temperature,topic_humidite,topic_pression,topic_coordonnees); //connecte le client 
	
	
	

    		
    	}
}
