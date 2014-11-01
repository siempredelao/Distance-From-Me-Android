package gc.david.dfm.daogenerator;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * A Version of the Schema.
 * <p/>
 * Created by David on 29/10/2014.
 */
public abstract class SchemaVersion {

	public static final String CURRENT_SCHEMA_PACKAGE = "gc.david.dfm.model";
	private final Schema schema;
	private final boolean current;

	/**
	 * Constructor
	 *
	 * @param current indicating if this is the current schema.
	 */
	public SchemaVersion(final boolean current) {
		final int version = getVersionNumber();
		String packageName = CURRENT_SCHEMA_PACKAGE;
		if (!current) {
			packageName += ".v" + version;
		}
		this.schema = new Schema(version, packageName);
		this.schema.enableKeepSectionsByDefault();
		this.current = current;
	}

	/**
	 * @return the GreenDAO schema.
	 */
	protected Schema getSchema() {
		return schema;
	}

	protected Entity getEntity(final String entityName, final Schema schema) {

		for (final Entity entity : schema.getEntities()) {
			if (entity.getClassName().equals(entityName)) {
				return entity;
			}
		}
		return null;
	}

	protected void removeEntity(final String entityName, final Schema schema) {
		for (final Entity entity : schema.getEntities()) {
			if (entity.getClassName().equals(entityName)) {
				schema.getEntities().remove(entity);
				return;
			}
		}
	}

	protected void removeProperty(final String propertyName, final Entity entity) {
		for (final Property property : entity.getProperties()) {
			if (property.getPropertyName().equals(propertyName)) {
				entity.getProperties().remove(property);
				break;
			}
		}
	}

	/**
	 * @return boolean indicating if this is the highest or current schema version.
	 */
	public boolean isCurrent() {
		return current;
	}

	/**
	 * @return unique integer schema version identifier.
	 */
	public abstract int getVersionNumber();
}
