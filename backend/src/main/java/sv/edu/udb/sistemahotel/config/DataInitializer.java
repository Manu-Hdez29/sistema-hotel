package sv.edu.udb.sistemahotel.config;

import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.model.Habitacion;
import sv.edu.udb.sistemahotel.model.Hotel;
import sv.edu.udb.sistemahotel.model.Huesped;
import sv.edu.udb.sistemahotel.model.PoliticaCancelacion;
import sv.edu.udb.sistemahotel.model.Rol;
import sv.edu.udb.sistemahotel.model.Servicio;
import sv.edu.udb.sistemahotel.model.Sucursal;
import sv.edu.udb.sistemahotel.model.Tarifa;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.TipoServicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    // Precio base por noche de cada tipo (en la sucursal con factor 1.00)
    private static final Map<NombreTipoHabitacion, BigDecimal> PRECIO_BASE = Map.of(
            NombreTipoHabitacion.INDIVIDUAL, new BigDecimal("35.00"),
            NombreTipoHabitacion.DOBLE, new BigDecimal("55.00"),
            NombreTipoHabitacion.SUITE, new BigDecimal("120.00"),
            NombreTipoHabitacion.FAMILIAR, new BigDecimal("90.00"));

    private final EntityManager em;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EntityManager em, PasswordEncoder passwordEncoder) {
        this.em = em;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Long usuarios = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
        if (usuarios > 0) {
            return;
        }

        // Roles (ids 1 a 3)
        Rol rolAdmin = obtenerORegistrarRol("ADMIN", "Administrador general del sistema");
        Rol rolRecepcion = obtenerORegistrarRol("RECEPCIONISTA", "Atiende reservas, pagos y reclamos");
        Rol rolHuesped = obtenerORegistrarRol("HUESPED", "Cliente que reserva habitaciones");

        // Tipos de habitación
        Map<NombreTipoHabitacion, TipoHabitacion> tipos = new HashMap<>();
        tipos.put(NombreTipoHabitacion.INDIVIDUAL, crearTipo(NombreTipoHabitacion.INDIVIDUAL, 1, "Una cama personal"));
        tipos.put(NombreTipoHabitacion.DOBLE, crearTipo(NombreTipoHabitacion.DOBLE, 2, "Cama matrimonial o dos camas"));
        tipos.put(NombreTipoHabitacion.SUITE, crearTipo(NombreTipoHabitacion.SUITE, 4, "Sala separada y jacuzzi"));
        tipos.put(NombreTipoHabitacion.FAMILIAR, crearTipo(NombreTipoHabitacion.FAMILIAR, 6, "Tres camas y espacio amplio"));

        // Hoteles
        Hotel plaza = crearHotel("Hotel Plaza Central", "0614-000000-000-1");
        Hotel bahia = crearHotel("Hotel Marea Azul", "0614-000000-000-2");
        Hotel volcan = crearHotel("Hotel Cumbre Verde", "0614-000000-000-3");

        // Sucursales por ciudad
        Sucursal sanSalvador = crearSucursal(plaza, "Sucursal San Salvador", "San Salvador", "Calle Principal #100", "2222-1000");
        Sucursal santaTecla = crearSucursal(plaza, "Sucursal Santa Tecla", "Santa Tecla", "Calle Antigua #12", "2228-3000");
        Sucursal santaAna = crearSucursal(plaza, "Sucursal Santa Ana", "Santa Ana", "Av. Libertad #45", "2440-2000");
        Sucursal sonsonate = crearSucursal(bahia, "Sucursal Sonsonate", "Sonsonate", "Blvd. del Mar, km 5", "2450-4000");
        Sucursal laLibertad = crearSucursal(bahia, "Sucursal La Libertad", "La Libertad", "Carretera al Puerto, km 32", "2306-1000");
        Sucursal sanMiguel = crearSucursal(volcan, "Sucursal San Miguel", "San Miguel", "Av. Roosevelt #7", "2669-2000");
        Sucursal usulutan = crearSucursal(volcan, "Sucursal Usulután", "Usulután", "Calle Madero #20", "2605-3000");

        // Tarifas: el factor cambia el precio según la sucursal (playa más cara, oriente más barato)
        crearTarifas(sanSalvador, "1.00", tipos);
        crearTarifas(santaTecla, "0.95", tipos);
        crearTarifas(santaAna, "1.00", tipos);
        crearTarifas(sonsonate, "1.30", tipos);
        crearTarifas(laLibertad, "1.20", tipos);
        crearTarifas(sanMiguel, "0.90", tipos);
        crearTarifas(usulutan, "0.85", tipos);

        // Habitaciones: "número TIPO" y, opcional, el estado (MANTENIMIENTO)
        crearHabitaciones(sanSalvador, tipos,
                "101 INDIVIDUAL", "102 DOBLE", "201 DOBLE", "202 SUITE", "301 FAMILIAR", "302 DOBLE MANTENIMIENTO");
        crearHabitaciones(santaTecla, tipos,
                "101 INDIVIDUAL", "102 DOBLE", "201 SUITE");
        crearHabitaciones(santaAna, tipos,
                "101 INDIVIDUAL", "102 FAMILIAR", "201 DOBLE", "202 DOBLE");
        crearHabitaciones(sonsonate, tipos,
                "101 DOBLE", "102 SUITE", "201 FAMILIAR", "202 INDIVIDUAL");
        crearHabitaciones(laLibertad, tipos,
                "101 INDIVIDUAL", "102 DOBLE", "103 DOBLE", "201 SUITE");
        crearHabitaciones(sanMiguel, tipos,
                "101 INDIVIDUAL", "102 DOBLE", "201 FAMILIAR");
        crearHabitaciones(usulutan, tipos,
                "101 DOBLE", "102 DOBLE", "201 INDIVIDUAL", "202 SUITE MANTENIMIENTO");

        // Servicios adicionales
        crearServicio("Desayuno buffet", "8.50", TipoServicio.ALIMENTACION);
        crearServicio("Traslado al aeropuerto", "25.00", TipoServicio.TRANSPORTE);
        crearServicio("Lavandería express", "6.00", TipoServicio.LAVANDERIA);
        crearServicio("Masaje relajante", "40.00", TipoServicio.SPA);
        crearServicio("Cuna para bebé", "0.00", TipoServicio.OTRO);

        // Políticas de cancelación por hotel: a más días de anticipación, menos cargo
        for (Hotel hotel : List.of(plaza, bahia, volcan)) {
            crearPolitica(hotel, 7, "0.00", "Cancelación con 7 días o más de anticipación: sin cargo");
            crearPolitica(hotel, 2, "50.00", "Cancelación entre 2 y 6 días antes: cargo del 50%");
            crearPolitica(hotel, 0, "100.00", "Cancelación con menos de 2 días: cargo del 100%");
        }

        // Usuarios de prueba (ids 1 a 3)
        crearUsuario(rolAdmin, "Administrador", "admin@hotel.sv", "Admin123*");
        crearUsuario(rolRecepcion, "Recepcionista", "recepcion@hotel.sv", "Recep123*");

        Usuario usuarioHuesped = crearUsuario(rolHuesped, "Huésped", "huesped@hotel.sv", "Huesped123*");

        // Cada huésped también tiene su fila en HUESPED, igual que en el registro
        Huesped huesped = new Huesped();
        huesped.setUsuario(usuarioHuesped);
        huesped.setNombreCompleto(usuarioHuesped.getNombreCompleto());
        huesped.setFechaNacimiento(LocalDate.of(1995, 5, 10));
        huesped.setDocumentoIdentidad("04400000-3");
        huesped.setTelefono("7060-6070");
        huesped.setEmail(usuarioHuesped.getEmail());
        em.persist(huesped);
    }

    private Rol obtenerORegistrarRol(String nombre, String descripcion) {
        List<Rol> existentes = em.createQuery(
                        "SELECT r FROM Rol r WHERE r.nombreRol = :nombre", Rol.class)
                .setParameter("nombre", nombre)
                .getResultList();
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }
        Rol rol = new Rol();
        rol.setNombreRol(nombre);
        rol.setDescripcion(descripcion);
        em.persist(rol);
        return rol;
    }

    private Hotel crearHotel(String nombre, String nitRuc) {
        Hotel hotel = new Hotel();
        hotel.setNombre(nombre);
        hotel.setNitRuc(nitRuc);
        hotel.setEstado(EstadoGeneral.ACTIVO);
        em.persist(hotel);
        return hotel;
    }

    private Sucursal crearSucursal(Hotel hotel, String nombre, String ciudad, String direccion, String telefono) {
        Sucursal sucursal = new Sucursal();
        sucursal.setHotel(hotel);
        sucursal.setNombreSucursal(nombre);
        sucursal.setCiudad(ciudad);
        sucursal.setDireccion(direccion);
        sucursal.setTelefono(telefono);
        em.persist(sucursal);
        return sucursal;
    }

    private TipoHabitacion crearTipo(NombreTipoHabitacion nombre, int capacidad, String descripcion) {
        TipoHabitacion tipo = new TipoHabitacion();
        tipo.setNombre(nombre);
        tipo.setCapacidadMaxima(capacidad);
        tipo.setDescripcion(descripcion);
        em.persist(tipo);
        return tipo;
    }

    // Tres periodos por tipo: temporada normal 2026, temporada alta de fin de año (+30%) y normal 2027 (+5%)
    private void crearTarifas(Sucursal sucursal, String factor, Map<NombreTipoHabitacion, TipoHabitacion> tipos) {
        BigDecimal multiplicador = new BigDecimal(factor);
        for (Map.Entry<NombreTipoHabitacion, BigDecimal> entrada : PRECIO_BASE.entrySet()) {
            TipoHabitacion tipo = tipos.get(entrada.getKey());
            BigDecimal normal2026 = entrada.getValue().multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);
            BigDecimal altaFinAnio = normal2026.multiply(new BigDecimal("1.30")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal normal2027 = normal2026.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);

            guardarTarifa(sucursal, tipo, normal2026, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 30));
            guardarTarifa(sucursal, tipo, altaFinAnio, LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 15));
            guardarTarifa(sucursal, tipo, normal2027, LocalDate.of(2027, 1, 16), null);
        }
    }

    private void guardarTarifa(Sucursal sucursal, TipoHabitacion tipo, BigDecimal precio,
                               LocalDate inicio, LocalDate fin) {
        Tarifa tarifa = new Tarifa();
        tarifa.setSucursal(sucursal);
        tarifa.setTipoHabitacion(tipo);
        tarifa.setPrecioNoche(precio);
        tarifa.setFechaInicioVigencia(inicio);
        tarifa.setFechaFinVigencia(fin);
        em.persist(tarifa);
    }

    private void crearHabitaciones(Sucursal sucursal, Map<NombreTipoHabitacion, TipoHabitacion> tipos,
                                   String... specs) {
        for (String spec : specs) {
            String[] partes = spec.split(" ");
            Habitacion habitacion = new Habitacion();
            habitacion.setSucursal(sucursal);
            habitacion.setTipoHabitacion(tipos.get(NombreTipoHabitacion.valueOf(partes[1])));
            habitacion.setNumeroHabitacion(partes[0]);
            habitacion.setEstado(partes.length > 2
                    ? EstadoHabitacion.valueOf(partes[2])
                    : EstadoHabitacion.DISPONIBLE);
            em.persist(habitacion);
        }
    }

    private void crearServicio(String nombre, String precio, TipoServicio tipo) {
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setPrecio(new BigDecimal(precio));
        servicio.setTipoServicio(tipo);
        em.persist(servicio);
    }

    private void crearPolitica(Hotel hotel, int diasPrevios, String porcentaje, String descripcion) {
        PoliticaCancelacion politica = new PoliticaCancelacion();
        politica.setHotel(hotel);
        politica.setDiasPrevios(diasPrevios);
        politica.setPorcentajeCargo(new BigDecimal(porcentaje));
        politica.setDescripcion(descripcion);
        em.persist(politica);
    }

    private Usuario crearUsuario(Rol rol, String nombre, String email, String passwordPlano) {
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setNombreCompleto(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(passwordPlano));
        usuario.setEstado(EstadoGeneral.ACTIVO);
        em.persist(usuario);
        return usuario;
    }
}