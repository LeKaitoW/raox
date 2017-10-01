package ru.bmstu.rk9.rao.lib.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.jpa.HibernatePersistenceProvider;

import com.querydsl.jpa.impl.JPAQuery;

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

	private EntityManager entityManager;

	public SqlDataProvider(String driver, String url, String user, String password, Class<?> entity,
			Class<?>... entities) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		this.entities = new ArrayList<>(Arrays.asList(entities));
		this.entities.add(entity);
	}

	public SqlDataProvider setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
		return this;
	}

	public <T> JPAQuery<T> getQuery() {
		JPAQuery<T> query = new JPAQuery<T>(getEntityManager());
		return query;
	}

	public EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = createEntityManager();
		}
		return entityManager;
	}

	private EntityManager createEntityManager() {
		try {
			ClassLoader modelClassLoader = entities.get(0).getClassLoader();
			Map<String, Object> properties = new HashMap<>();
			properties.put(PROPERTY_PERSISTENCE_DRIVER, driver);
			properties.put(PROPERTY_PERSISTENCE_LOADED_CLASSES, entities);
			properties.put(PROPERTY_PERSISTENCE_URL, url);
			properties.put(PROPERTY_PERSISTENCE_USER, user);
			properties.put(PROPERTY_PERSISTENCE_PASSWORD, password);

			PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfoImpl(persistenceUnitName,
					modelClassLoader);
			EntityManagerFactory emf = new HibernatePersistenceProvider()
					.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
			return emf.createEntityManager();

		} catch (org.hibernate.exception.JDBCConnectionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
