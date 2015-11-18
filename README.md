# AsyncChat
#The project is an asynchronous chat, which allows sending messages over the network through a server.


Проект реализован с помощью клиент-серверной архитектуры и использования Multithreading.  
Класс Server - ожидает подключения по каналу (ServerSocketChannel), после чего создаёт экземпляр класса ClientHandler, 
через который и происходит взаимодействие клиента с сервером. 
Класс Client - desktop application, реализован с помощью JavaFx, присоединяется к серверу, после чего можно отправлять и 
принимать сообщения от других клиентов. 
