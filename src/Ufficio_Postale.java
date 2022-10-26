import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;


public class Ufficio_Postale {

    public static class Persona implements Runnable { // Task
        private int numero;

        public Persona(int numero) {
            this.numero = numero;
        }

        public void run() {
            Random rd = new Random();
            int value  = rd.nextInt(1 + 10) + 1;
            try {
                TimeUnit.MILLISECONDS.sleep(value);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Il cliente con il numero " + this.numero + " e' stato servito in " + value + " secondi!");
        }

    }


    public static class Ufficio {

        private int numero_sportelli;
        private int k;
        private BlockingQueue<Runnable> q;
        private ThreadPoolExecutor service;

        public Ufficio(int numero_sportelli, int k) {
            this.numero_sportelli = numero_sportelli;
            this.k = k;
            this.q = new LinkedBlockingQueue<Runnable>(k);
            this.service = new ThreadPoolExecutor(numero_sportelli, numero_sportelli, 0L, TimeUnit.MILLISECONDS, this.q);
            service.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // blocca finché non riesce ad aggiungere
                    try {
                        //System.out.println("Sala di attesa piena!");
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        public void serviClienti(LinkedBlockingQueue<Persona> codaSalaAttesa) {

            Persona val;

            while (!codaSalaAttesa.isEmpty()) {
                val = codaSalaAttesa.peek();
                try {
                    service.execute(val);
                } catch (RejectedExecutionException ex) {}
                codaSalaAttesa.remove();
            }

            service.shutdown();

            try {
                if (!service.awaitTermination(60000, TimeUnit.SECONDS)) {
                    System.err.println("I thread non sono terminati entro i tempi!");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Tutti i clienti sono stati serviti!");
        }


    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Inserisci il numero di persone da servire");
        int n = sc.nextInt();

        int n_sportelli = 4, k = 10;

        Ufficio u = new Ufficio(n_sportelli, k);
        LinkedBlockingQueue<Persona> q = new LinkedBlockingQueue<Persona>();

        for (int i = 1; i <= n; i++) {
            q.add(new Persona(i));
        }

        u.serviClienti(q);

        System.exit(0);

    }

}
