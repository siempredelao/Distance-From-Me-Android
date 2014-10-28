package gc.david.dfm.model.v1;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

public class EntryTest extends AbstractDaoTestLongPk<EntryDao, Entry> {

    public EntryTest() {
        super(EntryDao.class);
    }

    @Override
    protected Entry createEntity(Long key) {
        Entry entity = new Entry();
        entity.setId(key);
        entity.setNombre("nombre");
        entity.setLat_origen(0);
        entity.setLon_origen(0);
        entity.setLat_destino(0);
        entity.setLon_destino(0);
        entity.setDistancia("distancia");
        entity.setFecha("fecha");
        return entity;
    }

}
