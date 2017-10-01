package ru.bmstu.rk9.rao.lib.persistence;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

// Persistence loading without META-INF/persistence.xml
// https://stackoverflow.com/questions/1989672/create-jpa-entitymanager-without-persistence-xml-configuration-file
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

	private static final String PERSISTENCE_PROVIDER = "org.hibernate.jpa.HibernatePersistenceProvider";
	private final String persistenceUnitName;
	private final ClassLoader classLoader;

	public PersistenceUnitInfoImpl(String persistenceUnitName, ClassLoader classLoader) {
		this.persistenceUnitName = persistenceUnitName;
		this.classLoader = classLoader;
	}

	@Override
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	@Override
	public String getPersistenceProviderClassName() {
		return PERSISTENCE_PROVIDER;
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return PersistenceUnitTransactionType.RESOURCE_LOCAL;
	}

	@Override
	public DataSource getJtaDataSource() {
		return null;
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return null;
	}

	@Override
	public List<String> getMappingFileNames() {
		return Collections.emptyList();
	}

	@Override
	public List<URL> getJarFileUrls() {
		return Collections.emptyList();
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		return null;
	}

	@Override
	public List<String> getManagedClassNames() {
		return Collections.emptyList();
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return true;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return SharedCacheMode.UNSPECIFIED;
	}

	@Override
	public ValidationMode getValidationMode() {
		return ValidationMode.AUTO;
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
		throw new UnsupportedOperationException();

	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		throw new UnsupportedOperationException();
	}
}
