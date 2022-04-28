-Το project δημιουργήθηκε σε περιβάλλον Intellij IDEA 2021.1 x64 .

-Αφού κατεβάσετε το library apache-tika (tika-app-1.25.jar) και μερικά βίντεο που χρησιμοποιήσαμε από το παρακάτω link:

VIDEOS		----> https://www.dropbox.com/s/2lieo7radd6lwgq/Videos.zip?dl=0
APACHE-TIKA 	----> https://www.dropbox.com/s/jjjzl9avrvxyayu/tika-app-1.25.jar?dl=0


-Tοποθετήστε τα βίντεο στους αντίστοιχους φακέλους ( PUB1 VIDEOS, PUB2 VIDEOS ) του project, 
διότι τα path του project είναι ρυθμισμένα να διαβάζουν .mp4 αρχεία από τους συγκεκριμένους φακέλους.

-Το project είναι φτιαγμένο για να λειτουργεί αφού δημιουργηθεί πρώτα το broker System , το οποίο αποτελείται απο 3 brokers.

-Η εφαρμογή γενικά έχει 3 main, την PublisherImpl, την BrokerImpl και την ConsumerImpl
Χρησιμοποιώντας λοιπόν Intellij , για να το τρέξουμε ακολουθούμε τα εξής βηματα:


	1)Με Program Arguments τον αριθμό 1, run την BrokerImpl
	2)Με Program Arguments τον αριθμό 2, run την BrokerImpl
	3)Με Program Arguments τον αριθμό 3, run την BrokerImpl

	
	4)Με Program Arguments τον αριθμό 1, run την PublisherImpl
	5)Με Program Arguments τον αριθμό 2, run την PublisherImpl

	6)Τέλος, κάνουμε run την ConsumerImpl,  λαμβάνουμε δεδομένα από το BrokerSystem 
	και στην συνέχεια με βάση την λίστα που θα εμφανιστεί επιλέγουμε τι είδους βίντεο θέλουμε να δούμε.


-Τα ονόματα των hashtag ή των ChannelName που επιθυμούμε να δούμε τα κάνουμε copy - paste ή τα πληκτρολογούμαι όπως θα μας εμφανίζονται στην λιστά για αποφυγή τυχον σφαλμάτων!!

-Για οποιαδήποτε διευκρίνιση ή απορία επικοινωνήστε με ένα απο τα παρακάτω email:

velis7_me@hotmail.com
stefanostakas@icloud.com
dionisisarvanitis@protonmail.com

Σας ευχαριστούμε πολύ.