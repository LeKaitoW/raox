package ru.bmstu.rk9.rao.lib.persistence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;

public final class SqlDataProvider implements DataProvider {

	private static final String PROPERTY_PERSISTENCE_PASSWORD = "javax.persistence.jdbc.password";
	private static final String PROPERTY_PERSISTENCE_USER = "javax.persistence.jdbc.user";
	private static final String PROPERTY_PERSISTENCE_URL = "javax.persistence.jdbc.url";
	private static final String PROPERTY_PERSISTENCE_DRIVER = "javax.persistence.jdbc.driver";
	private static final String PROPERTY_PERSISTENCE_LOADED_CLASSES = org.hibernate.jpa.AvailableSettings.LOADED_CLASSES;

	private String persistenceUnitName = "default";
	private final String driver;
	private final String url;
	private final String user;
	private final String password;
	private final List<Class<?>> entities;

	public SqlDataProvider(String driver, String url, String user, String password, Class<?>... entities) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		this.entities = Arrays.asList(entities);
	}

	public SqlDataProvider setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
		return this;
	}

	public EntityManager getEntityManager() throws ClassNotFoundException {
		Map<String, Object> properties = new HashMap<>();

		properties.put(PROPERTY_PERSISTENCE_DRIVER, driver);
		properties.put(PROPERTY_PERSISTENCE_LOADED_CLASSES, entities);
		properties.put(PROPERTY_PERSISTENCE_URL, url);
		properties.put(PROPERTY_PERSISTENCE_USER, user);
		properties.put(PROPERTY_PERSISTENCE_PASSWORD, password);

		properties.put(AvailableSettings.SHOW_SQL, true);

		try {
			// https://stackoverflow.com/questions/27304580/mapping-entities-from-outside-classpath-loaded-dynamically
			if (entities.size() > 0)
				Thread.currentThread().setContextClassLoader(entities.get(0).getClassLoader());

			PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfoImpl(persistenceUnitName);
			EntityManagerFactory emf = new HibernatePersistenceProvider()
					.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
			return emf.createEntityManager();

		} catch (org.hibernate.exception.JDBCConnectionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
