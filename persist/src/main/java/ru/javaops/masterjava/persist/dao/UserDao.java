package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class UserDao implements AbstractDao {

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user);
            user.setId(id);
        } else {
            insertWitId(user);
        }
        return user;
    }

    public void insertBatch(List<User> users, Integer batchSize) {
        if (batchSize==0) batchSize = 100;
        Integer finalBatchSize = batchSize;
        List<User> batchNew = new ArrayList<>();
        List<User> batchWithId = new ArrayList<>();

        users.forEach(user -> {
            if (user.isNew()){
                batchNew.add(user);
            }
            else{
                batchWithId.add(user);
            }
            insertBatchByCondition(batchNew, batchNew.size()==finalBatchSize);
            insertBatchByCondition(batchWithId, batchWithId.size()==finalBatchSize);
        });
        insertBatchByCondition(batchNew, batchNew.size() > 0);
        insertBatchByCondition(batchWithId, batchWithId.size() > 0);
    }

    private void insertBatchByCondition(List<User> batch, Boolean condition) {
        if (condition) {
            insertUsers(batch);
            batch.clear();
        }
    }

    @SqlUpdate("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user);

    @SqlBatch("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT (email) DO NOTHING ;")
    abstract void insertUsers(@BindBean List<User> users);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ")
    abstract void insertWitId(@BindBean User user);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();
}
