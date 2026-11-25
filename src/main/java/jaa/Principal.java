package jaa;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Principal {

    public static void main(String[] args) {

        try (SessionFactory ss = new Configuration().configure().buildSessionFactory();
             Session sesion = ss.openSession()) {

            Long cochesGuardados = sesion.createQuery(
                    "select count(c) from Coche c", Long.class
            ).uniqueResult();

            if (cochesGuardados == 0) {
                Transaction tx = sesion.beginTransaction();

                try {
                    Marca opel = new Marca("Opel");
                    Marca seat = new Marca("Seat");
                    Marca toyota = new Marca("Toyota");
                    Marca volkswagen = new Marca("Volkswagen");

                    sesion.persist(opel);
                    sesion.persist(seat);
                    sesion.persist(toyota);
                    sesion.persist(volkswagen);
                    sesion.persist(new Marca("Renault"));

                    sesion.persist(new Coche(opel, "Astra", 150));
                    sesion.persist(new Coche(seat, "Ibiza", 110));
                    sesion.persist(new Coche(toyota, "Corolla", 140));
                    sesion.persist(new Coche(toyota, "Yaris", 116));
                    sesion.persist(new Coche(volkswagen, "Golf", 200));

                    tx.commit();
                } catch (RuntimeException e) {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                    throw e;
                }
            }

            System.out.println("\n1. Todos los coches por modelo");

            List<Coche> coches = sesion.createQuery(
                    "from Coche c order by c.modelo", Coche.class
            ).list();

            for (Coche coche : coches) {
                System.out.println(coche);
            }

            System.out.println("\n2. Coches de 150 CV o más");

            List<Coche> potentes = sesion.createQuery(
                    "from Coche c where c.potencia >= :minimo order by c.potencia desc",
                    Coche.class
            ).setParameter("minimo", 150).list();

            for (Coche coche : potentes) {
                System.out.println(coche);
            }

            Long total = sesion.createQuery(
                    "select count(c) from Coche c", Long.class
            ).uniqueResult();

            System.out.println("\n3. Total de coches: " + total);

            System.out.println("\n4. Coches de la marca Toyota");

            List<String> modelosToyota = sesion.createQuery(
                    "select c.modelo from Coche c join c.marca m " +
                    "where m.nombre = :marca order by c.modelo",
                    String.class
            ).setParameter("marca", "Toyota").list();

            for (String modelo : modelosToyota) {
                System.out.println(modelo);
            }

            System.out.println("\n5. Número de coches por marca");

            List<Object[]> cochesPorMarca = sesion.createQuery(
                    "select m.nombre, count(c.id) from Marca m " +
                    "left join Coche c on c.marca = m " +
                    "group by m.id, m.nombre " +
                    "order by count(c.id) desc, m.nombre",
                    Object[].class
            ).list();

            for (Object[] fila : cochesPorMarca) {
                System.out.println(fila[0] + ": " + fila[1]);
            }
        }
    }
}