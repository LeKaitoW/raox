package ru.bmstu.rk9.rao.lib.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.jpa.HibernatePersistenceProvider;

public final class RaoEntityManager {

	private static final String PROPERTY_PERSISTENCE_PASSWORD = "javax.persistence.jdbc.password";
	private static final String PROPERTY_PERSISTENCE_USER = "javax.persistence.jdbc.user";
	private static final String PROPERTY_PERSISTENCE_URL = "javax.persistence.jdbc.url";
	private static final String PROPERTY_PERSISTENCE_DRIVER = "javax.persistence.jdbc.driver";
	private static final String PROPERTY_PERSISTENCE_LOADED_CLASSES = org.hibernate.jpa.AvailableSettings.LOADED_CLASSES;

	private String driver = "com.mysql.jdbc.Driver";
	private String url = "jdbc:mysql://mikhailmineev.ru:3306/corpterminal";
	private String persistenceUnitName = "corpterminal";
	private String user = "jpademo";
	private String password = "5xYB2e6T5Jo7ajA";
	private List<Class<?>> loadedClasses = new ArrayList<>();

	public RaoEntityManager setDriver(String driver) {
		this.driver = driver;
		return this;
	}

	public RaoEntityManager setUrl(String url) {
		this.url = url;
		return this;
	}

	public RaoEntityManager setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
		return this;
	}

	public RaoEntityManager setUser(String user) {
		this.user = user;
		return this;
	}

	public RaoEntityManager setPassword(String password) {
		this.password = password;
		return this;
	}

	public RaoEntityManager addLoadedClasses(Class<?> loadedClass) {
		this.loadedClasses.add(loadedClass);
		return this;
	}

	public EntityManager createEntityManager() {
		Map<String, Object> properties = new HashMap<>();

		properties.put(PROPERTY_PERSISTENCE_DRIVER, driver);
		properties.put(PROPERTY_PERSISTENCE_LOADED_CLASSES, loadedClasses);
		properties.put(PROPERTY_PERSISTENCE_URL, url);
		properties.put(PROPERTY_PERSISTENCE_USER, user);
		properties.put(PROPERTY_PERSISTENCE_PASSWORD, password);

		// properties.put(AvailableSettings.DIALECT, Oracle12cDialect.class);
		// properties.put(AvailableSettings.HBM2DDL_AUTO, CREATE);
		// properties.put(AvailableSettings.SHOW_SQL, true);
		// properties.put(AvailableSettings.QUERY_STARTUP_CHECKING, false);
		// properties.put(AvailableSettings.GENERATE_STATISTICS, false);
		// properties.put(AvailableSettings.USE_REFLECTION_OPTIMIZER, false);
		// properties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, false);
		// properties.put(AvailableSettings.USE_QUERY_CACHE, false);
		// properties.put(AvailableSettings.USE_STRUCTURED_CACHE, false);
		// properties.put(AvailableSettings.STATEMENT_BATCH_SIZE, 20);

		try {
			// https://stackoverflow.com/questions/27304580/mapping-entities-from-outside-classpath-loaded-dynamically
			if (loadedClasses.size() > 0)
				Thread.currentThread().setContextClassLoader(loadedClasses.get(0).getClassLoader());

			PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfoImpl(persistenceUnitName);
			EntityManagerFactory emf = new HibernatePersistenceProvider()
					.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
			return emf.createEntityManager();

		} catch (org.hibernate.exception.JDBCConnectionException e) {
			// TODO хороший вывод об ошибках подключения
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
