package gc.david.dfm.model.v2;

import java.util.Date;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

public class DistanceTest extends AbstractDaoTestLongPk<DistanceDao, Distance> {

    public DistanceTest() {
        super(DistanceDao.class);
    }

    @Override
    protected Distance createEntity(Long key) {
        Distance entity = new Distance();
        entity.setId(key);
        entity.setName("name");
        entity.setDistance("distance");
        entity.setDate(new Date());
        return entity;
    }

}
