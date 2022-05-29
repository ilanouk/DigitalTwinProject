/// source : https://www.hivemq.com/blog/mqtt-client-library-encyclopedia-eclipse-paho-java/
/// http://ashishdoneriya.github.io/influxdb-java/





import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import java.util.Properties;
import java.io.FileInputStream;


public class lien_serv_rasp {


	public static void connecterClient(MqttClient client,String topic_test) throws Exception {   //Connection au MQTT et aux differents topics
	client.connect(); //Connection au MQTT
    		
    		if (client.isConnected()) {
    			System.out.println("Connecté"); 
    			
    			// Abonnement aux différents topics
    			
    			client.subscribe(topic_test,1);
    			
    			
    		
    		}
    		else{
    		System.out.println("Erreur de connexion");
    	
  		}

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

		String topic_test = appProps.getProperty("topic_test");
		String lienMqtt = appProps.getProperty("lienMqtt");
		String portMqtt = appProps.getProperty("portMqtt");
		
		
		
		//Configuration de la connexion au MQTT
		MqttClient client = new MqttClient(   
		lienMqtt+":"+portMqtt, 
    		MqttClient.generateClientId(), 
    		new MemoryPersistence()); 
    		
    		// Configuration de la connexion  à la base de donnée
    		
    		
    		
    		
    		
    		client.setCallback(new MqttCallback() {

    		@Override
    		public void connectionLost(Throwable cause) { //Lorsque la connexion est perdu
    		
    			System.out.println("Connexion perdue");
    		
    		}

    		@Override
    		
    		//Ecriture des données dans la base de donnée lorsqu'un message arrive
    		
    		public void messageArrived(String topic, MqttMessage message) throws Exception { //	Lorsque l'on reçoit un message
    			System.out.println(message + " publié sur le topic : "+ topic);
			
	
    		}
    		
    		
		@Override
    		public void deliveryComplete(IMqttDeliveryToken token) {
    		
    		}
	});
	
	connecterClient(client,topic_test); //connecte le client 
	
	
	

    		
    	}
}
