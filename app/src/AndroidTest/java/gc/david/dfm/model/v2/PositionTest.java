package gc.david.dfm.model.v2;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

public class PositionTest extends AbstractDaoTestLongPk<PositionDao, Position> {

    public PositionTest() {
        super(PositionDao.class);
    }

    @Override
    protected Position createEntity(Long key) {
        Position entity = new Position();
        entity.setId(key);
        entity.setLatitude(0);
        entity.setLongitude(0);
        entity.setDistanceId(0L);
        return entity;
    }

}
