-The project was created in Intellij IDEA 2021.1 x64 environment.

-After downloading the apache-tika library (tika-app-1.25.jar) and some videos 

-Place the videos in the respective folders ( PUB1 VIDEOS, PUB2 VIDEOS ) of the project, 
because the project paths are configured to read .mp4 files from these folders.

-The project is made to work after the broker System is created , which consists of 3 brokers.

-The application generally has 3 mains, PublisherImpl, BrokerImpl and ConsumerImpl
So using Intellij , to run it we follow the following steps:


	1)With Program Arguments number 1, run BrokerImpl
	2)With Program Arguments number 2, run BrokerImpl
	3)With Program Arguments number 3, run BrokerImpl

	
	4)With Program Arguments number 1, run PublisherImpl
	5)With Program Arguments number 2, run PublisherImpl

	6)Finally, we run ConsumerImpl, get data from BrokerSystem 
	and then based on the list that comes up we choose what kind of video we want to watch.

-The names of the hashtags or the ChannelName we wish to see, we copy - paste or type them ,as they will appear in the list to avoid any errors!!

-For any clarification or questions please contact the following email:

stavrianosvel@outlook.com