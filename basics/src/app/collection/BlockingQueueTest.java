package app.collection;

import app.runner.Run;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Run(active = false)
public class BlockingQueueTest {

    @Run(active = false)
    static void blockingQueueTest() throws InterruptedException {

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        // producer thread
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    int bound = randInt(10);
                    for (int j = 0; j < bound; j++) queue.put(i);
                    Thread.sleep(randInt(1000));
                }
                queue.put(-1); // for stopping the execution
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // consumer thread
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    int item = queue.take();
                    if (item == -1) break;
                    System.out.println("consumed item is " + item);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
        consumer.start();
        consumer.join();
    }

    static int randInt(int range) {
        Random random = new Random();
        return Math.abs(random.nextInt() % range);
    }
}
