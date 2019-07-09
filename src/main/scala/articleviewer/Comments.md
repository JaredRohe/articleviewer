Dear Elevio,

Thank you so much for the chance to complete this programming challenge!
I had fun building the app, and I hope you don't hesitate to ask if you have any
questions at all related to the code.

I did want to take the time to point your attention to a few lines of code.
You'll see a few occurrences where I call ```Await.response(futureVal, durationVal)```.
I want to make clear that I am aware this is a blocking call. It is a bit strange to spawn 
asynchronous threads only to almost immediately block execution until that thread has finished.
However, you'll see that I am printing loading messages to the console and then blocking.  This is simulating
a GUI where the user would be presented with a loading screen and then that loading screen would be replaced with the 
result from the asynchronous thread. But since a loading screen, rather a loading message, cannot be replaced in 
a CLI app, I block until the thread finishes so that the output to the console remains sequential. An asynchronous
backend for the purpose of this particular CLI app is over-engineered, but I did so to demonstrate my knowledge
of asynchronous programming. The methods that make these asynchronous calls could easily be leveraged in a 
more complicated backend system. All that is needed is to remove the blocking Await calls, and then to implement 
the onComplete callback methods to handle the results truly asynchronously. 


Thank you so much for your consideration!

Kindly,

Jared Rohe