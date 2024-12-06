

// ConsoleInterface.java - Interfaz de usuario en consola
import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.sql.*;

import java.sql.SQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProyectoFinal {
    public static void main(String[] args) {
        // Variables necesarias
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        MovieController movieController = null;

        // Intentar conectar con la base de datos
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "newpassword");
            movieController = new MovieController(connection);
            System.out.println("Conexión exitosa a la base de datos.");
            
            // Crear una película de ejemplo
            System.out.println("Creando película...");
            Language language = chooseLanguage(connection); // Elegir idioma
            Movie newMovie = new Movie("Inception", "A mind-bending thriller", 2010, language, connection);
            movieController.createMovie(newMovie);
            System.out.println("Película creada.");

            // Leer una película
            System.out.print("Ingrese el ID de la película para obtener: ");
            int filmIdToGet = scanner.nextInt();
            Movie movie = movieController.getMovie(filmIdToGet);
            if (movie != null) {
                System.out.println("Película encontrada:");
                movie.display();
            } else {
                System.out.println("Película no encontrada.");
            }

            // Actualizar una película
            System.out.print("Ingrese el ID de la película a actualizar: ");
            int filmIdToUpdate = scanner.nextInt();
            Movie movieToUpdate = movieController.getMovie(filmIdToUpdate);  // Obtener la película por ID

            if (movieToUpdate != null) {
                // Aquí puedes hacer cambios en la película, por ejemplo:
                movieToUpdate.setTitle("New Title"); // Puedes modificar otras propiedades si es necesario
                movieController.updateMovie(movieToUpdate);  // Llamar a update con el objeto Movie
                System.out.println("Película actualizada.");
            } else {
                System.out.println("Película no encontrada.");
            }


            // Eliminar una película
            System.out.print("Ingrese el ID de la película a eliminar: ");
            int filmIdToDelete = scanner.nextInt();
            movieController.deleteMovie(filmIdToDelete);
            System.out.println("Película eliminada.");
        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ha ocurrido un error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar la conexión
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }

        // Cerrar scanner
        scanner.close();
    }

    // Método para elegir un idioma
    public static Language chooseLanguage(Connection connection) throws SQLException {
        System.out.println("Selecciona un idioma:");

        // Cambiar a un ResultSet desplazable (scrollable)
        String query = "SELECT * FROM language";
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(query)) {

            int i = 1;
            // Mostrar los idiomas disponibles
            while (rs.next()) {
                System.out.println(i++ + ". " + rs.getString("name"));
            }

            // Solicitar al usuario la opción
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();

            // Ahora recorremos de nuevo el ResultSet desde el principio sin necesidad de `beforeFirst()`
            Language selectedLanguage = null;
            int count = 1;
            rs.beforeFirst(); // Esto es ahora posible porque el ResultSet es desplazable

            while (rs.next()) {
                if (count == choice) {
                    int languageId = rs.getInt("language_id");
                    String languageName = rs.getString("name");
                    selectedLanguage = new Language(languageId, languageName);
                    break;
                }
                count++;
            }

            // Si no se encuentra el idioma, lanzar un error
            if (selectedLanguage == null) {
                System.out.println("Idioma no válido.");
                return null;
            }

            return selectedLanguage;
        }
    }


}

abstract class Entity {
    protected Connection connection;

    public Entity(Connection connection) {
        this.connection = connection;
    }

    // Método para cerrar la conexión
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Método para preparar una sentencia SQL
    protected PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
}
// iDataPost.java - Interface para operaciones CRUD estándar.

interface iDataPost<T> {
    void create(T entity);     // Crear
    T read(int id);            // Leer
    void update(T entity);     // Actualizar
    void delete(int id);       // Eliminar
}

// DataContext.java - Clase abstracta híbrida para operaciones CRUD


abstract class DataContext<T> implements iDataPost<T> {

    // Método concreto de actualización
    public void update(T entity) {
        System.out.println("Actualizando la entidad...");
        // Aquí iría el código para actualizar la entidad en la base de datos.
    }

    // Método concreto de obtención de datos (sobrecargado para diferentes tipos de consultas)
    public T read(int id) {
        System.out.println("Leyendo la entidad con id: " + id);
        // Implementación de lógica de lectura de base de datos (consulta por id)
        return null;
    }

    public List<T> readAll() {
        System.out.println("Leyendo todas las entidades...");
        // Lógica para obtener todas las entidades
        return null;
    }

    // Método concreto de creación
    public void create(T entity) {
        System.out.println("Creando la entidad...");
        // Implementación de lógica para insertar la entidad en la base de datos
    }

    // Método concreto de eliminación
    public void delete(int id) {
        System.out.println("Eliminando la entidad con id: " + id);
        // Lógica para eliminar la entidad de la base de datos
    }

    // Métodos abstractos si se necesitan para clases derivadas
    public abstract void validateEntity(T entity);
}
// Movie.java - Clase modelo para Película


class Movie extends Entity {
    private int filmId;
    private String title;
    private String description;
    private int releaseYear;
    private Language language;  // Relación con la clase Language

    // Constructor
    public Movie(String title, String description, int releaseYear, Language language, Connection connection) {
        super(connection);
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.language = language;
    }

    // Getters y Setters
    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    // Método para guardar la película
    public void save() throws SQLException {
        String query = "INSERT INTO film (title, description, release_year, language_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, releaseYear);
            stmt.setInt(4, language.getLanguageId());  // Relacionar con el ID de Language
            stmt.executeUpdate();
        }
    }

    // Obtener película por ID
    public static Movie getById(int id, Connection connection) throws SQLException {
        String query = "SELECT * FROM film WHERE film_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Language language = Language.getById(rs.getInt("language_id"), connection);
                return new Movie(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("release_year"),
                        language,  // Obtenemos el idioma relacionado
                        connection
                );
            }
        }
        return null;
    }

    // Método para actualizar la película
    public void update() throws SQLException {
        String query = "UPDATE film SET title = ?, description = ?, release_year = ?, language_id = ? WHERE film_id = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, releaseYear);
            stmt.setInt(4, language.getLanguageId());
            stmt.setInt(5, filmId);
            stmt.executeUpdate();
        }
    }

    // Método para eliminar la película (marcar como inactiva)
    public void delete() throws SQLException {
        String query = "UPDATE film SET status = 'inactive' WHERE film_id = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, filmId);
            stmt.executeUpdate();
        }
    }

    // Método para mostrar la información completa de la película
    public void display() {
        System.out.println("Película: " + title);
        System.out.println("Descripción: " + description);
        System.out.println("Año de estreno: " + releaseYear);
        System.out.println("Idioma: " + language.getName());
        System.out.println("=====================================");
    }
}







class Language {

    private int languageId;
    private String name;

    // Constructor que recibe el ID y el nombre del idioma
    public Language(int languageId, String name) {
        this.languageId = languageId;
        this.name = name;
    }

    // Métodos getter para acceder a los atributos
    public int getLanguageId() {
        return languageId;
    }

    public String getName() {
        return name;
    }

    // Obtener idioma por ID desde la base de datos
    public static Language getById(int id, Connection connection) throws SQLException {
        String query = "SELECT * FROM language WHERE language_id = ?";
        
        // Cambiar el tipo de ResultSet a TYPE_SCROLL_INSENSITIVE o TYPE_SCROLL_SENSITIVE
        try (PreparedStatement stmt = connection.prepareStatement(query, 
                                                                 ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                                 ResultSet.CONCUR_READ_ONLY)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Crear un objeto Language con los datos de la base de datos
                return new Language(rs.getInt("language_id"), rs.getString("name"));
            }
        }
        return null;
    }

    // (Opcional) Para mostrar la información del idioma como un String
    @Override
    public String toString() {
        return "ID: " + languageId + ", Name: " + name;
    }
}







// MovieController.java - Controlador para gestionar películas

class MovieController {
    private Connection connection;

    public MovieController(Connection connection) {
        this.connection = connection;
    }

    public void createMovie(Movie movie) throws SQLException {
        movie.save();
    }

    public Movie getMovie(int filmId) throws SQLException {
        return Movie.getById(filmId, connection);
    }

    public void updateMovie(Movie movie) throws SQLException {
        if (movie != null) {
            movie.update();  // Llamar al método update de la clase Movie
        } else {
            System.out.println("La película no se puede actualizar porque es nula.");
        }
    }

    public void deleteMovie(int filmId) throws SQLException {
        Movie movie = Movie.getById(filmId, connection);
        if (movie != null) {
            movie.delete();
        }
    }
}


//InventoryController.java - Controlador para gestionar inventarios

class InventoryController {

    private Connection connection;

    // Constructor que recibe la conexión a la base de datos
    public InventoryController(Connection connection) {
        this.connection = connection;
    }

    public void createInventory(Inventory inventory) throws SQLException {
        inventory.save();
    }

    public Inventory getInventory(int inventoryId) throws SQLException {
        return Inventory.getById(inventoryId, connection);
    }

    public void updateInventory(Inventory inventory) throws SQLException {
        inventory.update();
    }

    public void deleteInventory(int inventoryId) throws SQLException {
        Inventory inventory = Inventory.getById(inventoryId, connection);
        if (inventory != null) {
            inventory.delete();
        }
    }
}

// Inventory.java - Clase modelo para Inventario

class Inventory extends Entity {

    private int inventoryId;
    private int filmId;
    private int storeId;
    private String status;

    // Constructor de Inventory con conexión
    public Inventory(int filmId, int storeId, String status, Connection connection) throws SQLException {
        super(connection);  // Llamada al constructor de Entity con la conexión
        this.filmId = filmId;
        this.storeId = storeId;
        this.status = status;
    }

    // Getters y Setters
    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Guardar inventario
    public void save() throws SQLException {
        String query = "INSERT INTO inventory (film_id, store_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, filmId);
            stmt.setInt(2, storeId);
            stmt.setString(3, status);
            stmt.executeUpdate();
        }
    }

    // Obtener inventario por ID
    public static Inventory getById(int id, Connection connection) throws SQLException {
        String query = "SELECT * FROM inventory WHERE inventory_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Inventory inventory = new Inventory(
                    rs.getInt("film_id"),
                    rs.getInt("store_id"),
                    rs.getString("status"),
                    connection
                );
                inventory.setInventoryId(rs.getInt("inventory_id"));
                return inventory;
            }
        }
        return null;
    }

    // Actualizar inventario
    public void update() throws SQLException {
        String query = "UPDATE inventory SET status = ? WHERE inventory_id = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, inventoryId);
            stmt.executeUpdate();
        }
    }

    // Eliminar inventario (marcar como inactivo)
    public void delete() throws SQLException {
        String query = "UPDATE inventory SET status = 'inactive' WHERE inventory_id = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, inventoryId);
            stmt.executeUpdate();
        }
    }
}


// ReportManager.java - Gestor de reportes y estadísticas


class ReportManager {

    public void generateMovieReport(List<Movie> movies) {
        System.out.println("Reporte de Películas:");
        movies.forEach(movie -> System.out.println(movie.getTitle()));
    }

    public void generateCSV(List<Movie> movies) {
        // Lógica para exportar a CSV
    }

    public void generateJSON(List<Movie> movies) {
        // Lógica para exportar a JSON
    }

    public Map<String, Double> generateStatistics(List<Movie> movies) {
        Map<String, Double> statistics = new HashMap<>();
        statistics.put("Total", (double) movies.size());
        // Agregar más métricas aquí
        return statistics;
    }
}

// MovieDataContext.java - Implementación del DataContext para películas

class MovieDataContext extends DataContext<Movie> {

    // Lista simulada para almacenar las películas
    private List<Movie> movies = new ArrayList<>();

    @Override
    public void create(Movie movie) {
        // Agregar la película a la lista (simulando la inserción en la base de datos)
        movies.add(movie);
        System.out.println("Película creada: " + movie.getTitle() + " con ID: " + movie.getFilmId());
    }

    @Override
    public Movie read(int id) {
        // Buscar la película por id en la lista
        for (Movie movie : movies) {
            if (movie.getFilmId() == id) {
                return movie;
            }
        }
        return null;
    }

    @Override
    public List<Movie> readAll() {
        // Retornar todas las películas
        return movies;
    }

    @Override
    public void update(Movie movie) {
        // Simular la actualización de la película en la lista
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getFilmId() == movie.getFilmId()) {
                movies.set(i, movie);
                System.out.println("Película actualizada: " + movie.getTitle());
                return;
            }
        }
        System.out.println("Película no encontrada.");
    }

    @Override
    public void delete(int id) {
        // Eliminar la película de la lista
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getFilmId() == id) {
                movies.remove(i);
                System.out.println("Película eliminada con ID: " + id);
                return;
            }
        }
        System.out.println("Película no encontrada.");
    }

    @Override
    public void validateEntity(Movie movie) {
        // Validaciones para asegurarse de que la película es válida
        if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            throw new IllegalArgumentException("El título de la película no puede estar vacío.");
        }
        // Puedes agregar más validaciones aquí, como comprobar el año de estreno, etc.
    }
}

class InventoryDataContext extends DataContext<Inventory> {

    // Lista simulada para almacenar el inventario
    private List<Inventory> inventories = new ArrayList<>();

    @Override
    public void create(Inventory inventory) {
        // Agregar inventario a la lista (simulando la inserción en la base de datos)
        inventories.add(inventory);
        System.out.println("Inventario creado con ID: " + inventory.getInventoryId());
    }

    @Override
    public Inventory read(int id) {
        // Buscar inventario por ID
        for (Inventory inventory : inventories) {
            if (inventory.getInventoryId() == id) {
                return inventory;
            }
        }
        return null;
    }

    @Override
    public List<Inventory> readAll() {
        // Retornar todos los inventarios
        return inventories;
    }

    @Override
    public void update(Inventory inventory) {
        // Simular la actualización del inventario en la lista
        for (int i = 0; i < inventories.size(); i++) {
            if (inventories.get(i).getInventoryId() == inventory.getInventoryId()) {
                inventories.set(i, inventory);
                System.out.println("Inventario actualizado con ID: " + inventory.getInventoryId());
                return;
            }
        }
        System.out.println("Inventario no encontrado.");
    }

    @Override
    public void delete(int id) {
        // Eliminar inventario de la lista
        for (int i = 0; i < inventories.size(); i++) {
            if (inventories.get(i).getInventoryId() == id) {
                inventories.remove(i);
                System.out.println("Inventario eliminado con ID: " + id);
                return;
            }
        }
        System.out.println("Inventario no encontrado.");
    }

    @Override
    public void validateEntity(Inventory inventory) {
        // Validación simple, como verificar si el ID y el estado son correctos
        if (inventory.getInventoryId() <= 0) {
            throw new IllegalArgumentException("El ID del inventario debe ser mayor que cero.");
        }
        if (inventory.getStatus() == null || inventory.getStatus().isEmpty()) {
            throw new IllegalArgumentException("El estado del inventario no puede estar vacío.");
        }
    }
}







/*com/sakila
|-- data
|   |-- MovieDataContext.java
|   |-- InventoryDataContext.java
|-- controllers
|   |-- MovieController.java
|   |-- InventoryController.java
|-- models
|   |-- Movie.java
|   |-- Inventory.java
|-- reports
|   |-- ReportManager.java
|-- ui
|   |-- ConsoleInterface.java
|-- DataContext.java
|-- iDataPost.java
*/















