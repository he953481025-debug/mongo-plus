import com.mongodb.BasicDBList;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @description:
 * @author: brandon
 * @date: 2021/11/13 16:32
 */
public class MongoQuery extends Query {

    public List<CriteriaDefinition> getAllCriteriaDefinition(){
        return super.getCriteria();
    }

    public void andOperator(List<CriteriaDefinition> list){
        BasicDBList basicDBList = createCriteriaList(list);
        registerCriteriaChainElement(Criteria.where("$and").is(basicDBList));
    }

    public void orOperator(List<CriteriaDefinition> list){
        BasicDBList basicDBList = createCriteriaList(list);
        registerCriteriaChainElement(Criteria.where("$or").is(basicDBList));
    }

    public void norOperator(List<CriteriaDefinition> list){
        BasicDBList basicDBList = createCriteriaList(list);
        registerCriteriaChainElement(Criteria.where("$nor").is(basicDBList));
    }


    private void registerCriteriaChainElement(Criteria criteria){
        if (lastOperatorWasNot()) {
            throw new IllegalArgumentException(
                    "operator $not is not allowed around criteria chain element: " + this.getQueryObject());
        }
        this.addCriteria(criteria);
    }



    private BasicDBList createCriteriaList(List<CriteriaDefinition> list) {
        BasicDBList bsonList = new BasicDBList();
        for (CriteriaDefinition c : list) {
            bsonList.add(c.getCriteriaObject());
        }
        return bsonList;
    }

    private boolean lastOperatorWasNot() {
        List<CriteriaDefinition> allCriteriaDefinition = getAllCriteriaDefinition();
        return !allCriteriaDefinition.isEmpty() && "$not".equals(allCriteriaDefinition.get(allCriteriaDefinition.size()-1).getKey());
    }
}
