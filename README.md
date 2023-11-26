# Swift Financial Messages

Swift financial messages play a vital role in facilitating global financial transactions by providing a standardized format for secure and reliable communication between financial institutions. They enhance efficiency, reduce operational risks, and enable seamless integration within the global financial ecosystem.


# SAA SOAP Host Adapter

SOAP Host Adapter Operations for message exchange. Swift Alliance Access SOAP Host Adapter is a component or service that facilitates the integration of Swift Alliance Access, a messaging platform provided by SWIFT, with applications or systems that communicate using SOAP (Simple Object Access Protocol). The Swift Alliance Access SOAP Host Adapter acts as an intermediary between the Swift Alliance Access platform and SOAP-based applications, enabling seamless communication and data exchange between them.

• Protocol Translation: The adapter translates SOAP messages received from SOAP-based applications into a format compatible with Swift Alliance Access, allowing the messages to be processed within the SWIFT network.

• Message Transformation: It performs any necessary data transformations or mappings to ensure the SOAP messages conform to the message formats and standards supported by Swift Alliance Access.

• Security and Authentication: The adapter handles security aspects, such as authentication and encryption, to ensure the integrity and confidentiality of the communication between SOAP-based applications and Swift Alliance Access.

• Error Handling and Logging: It provides mechanisms for handling errors, logging activities, and generating relevant alerts or notifications to facilitate troubleshooting and monitoring of the integration.


![SWIFT 1001](https://github.com/mohsenTalal/swift-financial-messages/assets/27899749/cb0049b7-1242-4a46-9d86-8b145bfe53dd)



# Table of contents
<!--ts-->
* [Open Connection](#open-connection)
* [Get Ack](#get-ack)
* [Ack](#ack)
* [Put](#put)
* [Close Connection](#close-connection)
<!--te-->

# Open Connection

To initiate a new session using the Swift SOAP adapter, you can follow these general steps:

* Set up the Environment: Ensure that you have the necessary infrastructure and software in place to connect to the Swift SOAP adapter. This may include the installation and configuration of the required middleware or integration platform.

* Establish Connection Credentials: Obtain the necessary connection credentials, such as a username and password, API key, or digital certificate, depending on the authentication mechanism used by the Swift SOAP adapter.

* Create a Session Request: Prepare a session initiation request message according to the specifications provided by the Swift SOAP adapter. This message typically includes details such as your organization's identification, connection parameters, and any additional settings required to establish the session.

* Send the Session Request: Use the appropriate method or API provided by the Swift SOAP adapter to send the session initiation request. This typically involves making a SOAP (Simple Object Access Protocol) request to the designated endpoint or URL.

* Handle Response: Once the session initiation request is sent, wait for the response from the Swift SOAP adapter. The response may indicate the success or failure of the session initiation attempt. If successful, the response may include session details or a session token that can be used for subsequent operations.

* Manage the Session: If the session initiation is successful, you can proceed with using the established session to perform the desired operations, such as sending and receiving Swift financial messages, retrieving transaction statuses, or managing secure communication with other financial institutions
```bash
OpenResponseDetails response = service.open(messagePartner, 1L, 10L,

Direction.TO_AND_FROM_MESSAGE_PARTNER, RoutingMode.IMMEDIATE, null);
```

# Get Ack

To retrieve the ACK for a Swift message, you typically follow these steps:

* Message Reference: Identify the unique message reference (also known as the Message Reference Number or MIR) assigned to the original Swift message for which you want to obtain the ACK. This reference number is generated when the message is sent.

* Query Options: Determine the available options for querying the ACK. This can vary depending on the system or interface you are using to send and receive Swift messages. Common methods include using APIs, integration platforms, or Swift messaging services.

* Query or Search: Utilize the appropriate query or search functionality provided by your Swift messaging system to search for the ACK associated with the message reference. This may involve using search parameters such as the message type, direction (incoming or outgoing), sender or receiver information, date/time range, or specific fields within the message.

* Retrieve the ACK: Once the search is performed, retrieve the ACK message or acknowledgement status associated with the original message reference. The ACK may be available as a separate message or as part of the message status or history details.

* Analyze the ACK: Review the ACK to determine its content and status. The ACK typically confirms the successful delivery and acceptance of the original message. It may contain information such as the ACK message type, transaction status, processing information, or any applicable error or rejection codes if the message was not successfully processed.

```bash
ObjectFactory factory = new ObjectFactory();

GetAck getAck = factory.createGetAck();

getAck.setTimeout(100L);

GetAckResponse response = service.getAck(getAck);
```

# Ack

In Swift financial messages, an "ACK" refers to an acknowledgment message that confirms the successful receipt and processing of a specific financial message. The ACK serves as a confirmation to the sender that their message was received by the intended recipient and was processed without any errors or exceptions.

The ACK message in Swift typically contains information such as:

* Message Reference: The unique reference number or code assigned to the original financial message for which the ACK is being sent. This allows the sender to match the ACK with the corresponding original message.

* Message Type: The type of message being acknowledged, such as a payment message (MT103), an advice message (MT292), or any other relevant Swift message type.

* Status: Indicates the processing status of the original message. It confirms whether the message was successfully processed, rejected, or requires further action.

* Acknowledgment Codes: These codes provide more specific information about the status of the message, indicating if there were any issues or exceptions encountered during processing.

* Timestamps: The date and time when the ACK message was generated, allowing for tracking and auditing purposes.

* Sender and Receiver Information: The identification codes or names of the sending and receiving financial institutions involved in the message exchange.


# Put

To put a Swift message into Swift, you need to follow the standard messaging process established by the Society for Worldwide Interbank Financial Telecommunication (SWIFT). Here are the general steps involved:

* Prepare the Swift Message: Create the Swift message according to the specific message type and format required for your financial transaction. Swift messages are formatted using the SWIFT MT message standards, such as MT103 for Single Customer Credit Transfer or MT202 for General Financial Institution Transfer. Ensure that the message contains accurate and complete information, including sender and receiver details, transaction details, and any required codes or identifiers.

* Validate the Message: Validate the content and structure of the Swift message to ensure compliance with SWIFT standards and any applicable business rules. This includes verifying the presence of mandatory fields, adherence to field formats, and adherence to any message-specific guidelines or regulations.

* Connect to SWIFT Network: Establish a connection to the SWIFT network through your financial institution or a SWIFT service provider. This connection can be achieved using SWIFT-enabled software, messaging interfaces, or SWIFTNet connectivity solutions.

* Authenticate and Send the Message: Authenticate your identity and credentials through the established SWIFT connection. Once authenticated, transmit the Swift message through the secure SWIFT network to the intended recipient's financial institution. This can typically be done by using SWIFT messaging protocols, such as FIN (Financial Messaging), or XML-based formats like ISO 20022.

* Track and Confirm Delivery: Monitor the status of the message to track its progress through the SWIFT network. You can utilize SWIFT-specific tracking tools or rely on notifications and acknowledgments received from the recipient's financial institution. These notifications may include delivery confirmations, rejection notices, or other message status updates.
```bash
Put put = factory.createPut();
put.setAny(document.getDocumentElement());
PutResponse response = service.put(put);
```

# Close Connection

To close a connection in the Swift Alliance Access SOAP adapter, ust write these lines of code.
```csharp
Close close = factory.createClose();
close.setRoutingAction(RoutingAction.COMMIT);
CloseResponse response = service.close(close);
```

# Support

Need help or wanna share your thoughts? Don't hesitate to join us on Gitter or ask your question on StackOverflow:

>  StackOverflow: https://stackexchange.com/users/13936221/abdul-mohsen-al-enazi


# Contributors

>  swift-financial-messages is actively maintained by **[Mohsen Talal](https://github.com/mohsenTalal)**. Contributions are welcome and can be submitted using pull requests.

