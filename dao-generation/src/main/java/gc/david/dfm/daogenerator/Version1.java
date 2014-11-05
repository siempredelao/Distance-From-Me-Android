package gc.david.dfm.daogenerator;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by David on 29/10/2014.
 */
public class Version1 extends SchemaVersion {

    private static final String ENTRY_NAME = "Entry";

    /**
     * Constructor
     *
     * @param current
     */
    public Version1(boolean current) {
        super(current);

        final Schema schema = getSchema();

        addEntryEntity(schema);
    }

    private void addEntryEntity(Schema schema) {
        final Entity entry = schema.addEntity(ENTRY_NAME);

        entry.addIdProperty().autoincrement();
        entry.addStringProperty("nombre").notNull();
        entry.addDoubleProperty("lat_origen").notNull();
        entry.addDoubleProperty("lon_origen").notNull();
        entry.addDoubleProperty("lat_destino").notNull();
        entry.addDoubleProperty("lon_destino").notNull();
        entry.addStringProperty("distancia").notNull();
        entry.addStringProperty("fecha").notNull();
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }
}
