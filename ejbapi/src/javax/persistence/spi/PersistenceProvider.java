/**
 * The interface javax.persistence.spi.PersistenceProvider is 
 * implemented by the persistence provider, and is specified 
 * in the persistence.xml file in the persistence archive. It 
 * is invoked by the container when it needs to create an 
 * EntityManagerFactory, or by the javax.persistence.Persistence 
 * class when running outside the container.
 */
package javax.persistence.spi;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * @author Rick George
 *
 * Interface implemented by a persistence provider.
 * The implementation of this interface that is to
 * be used for a given EntityManager is specified in
 * persistence.xml file in the persistence archive.
 * This interface is invoked by the Container when it
 * needs to create an EntityManagerFactory, or by the
 * Persistence class when running outside the Container.
 */
public interface PersistenceProvider {

	/**
	 * Called by Persistence class when an EntityManagerFactory
	 * is to be created.
	 *
	 * @param emName The name of the EntityManager configuration
	 * for the factory
	 * @param map A Map of properties that may be used by the
	 * persistence provider
	 * @return EntityManagerFactory for the named EntityManager,
	 * or null if the provider is not the right provider
	 */
	public EntityManagerFactory createEntityManagerFactory(
									String emName, Map map);
	/**
	 * Called by the container when an EntityManagerFactory
	 * is to be created.
	 *
	 * @param info Metadata needed by the provider
	 * @return EntityManagerFactory for the named EntityManager
	 */
	public EntityManagerFactory createContainerEntityManagerFactory(
										PersistenceInfo info);
	}
