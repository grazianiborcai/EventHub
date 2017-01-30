# EventHub
Send message to your Event Hub from an Android application

Azure Event Hubs is a highly scalable publish-subscribe service that can ingest millions of events per second and stream them into multiple applications. This lets you process and analyze the massive amounts of data produced by your connected devices and applications. Once Event Hubs has collected the data, you can retrieve, transform and store it by using any real-time analytics provider or with batching/storage adapters.


## Overview

This Android library for Azure Event Hubs allows sending events to an Azure Event Hub. 


## Using the library


## Publishing Events

The vast majority of Event Hub applications using this and other client libraries are and will be event publishers. And for most of these publishers, publishing events is extremely simple.

With your Android application referencing this Android library, which is quite simple in a Maven build as we explain in the guide, you'll need to import the com.example.eventhub package with the EventHub class.


    import com.microsoft.azure.eventhubs.*;
    
Using an Event Hub connection string, which holds all required connection information, including an authorization key or token, you then create an EventHubClient instance, which manages a secure AMQP 1.0 connection to the Event Hub.

    final String namespaceName = "----ServiceBusNamespaceName-----";
    final String eventHubName = "----EventHubName-----";
    final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
    final String sasKey = "---SharedAccessSignatureKey----";

Now it is only necessary to create an AsyncTask EventHub and send your String message. You need to inform your Activity Context which can be "this" or "getContext()" if you are using a Fragment class.
    
    new EventHub(this)
            .setmNameSpace(namespaceName)
            .setmEventHub(eventHubName)
            .setmHubSasKeyName(sasKeyName)
            .setmHubSasKeyValue(sasKey).send("{"device":"android","sensor":"audio","ampliude":3.87}");

