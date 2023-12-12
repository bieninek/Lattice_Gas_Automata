import view.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main (String[] argv) throws InterruptedException, IOException {
        MainUI ui = new MainUI();

        while (true) {
            while (!ui.ifClicked()) {
                TimeUnit.MILLISECONDS.sleep(1000);
            }
            ui.simulate();
        }
    }
}
