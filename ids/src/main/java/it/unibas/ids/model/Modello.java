package it.unibas.ids.model;

import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class Modello {
    private Map<String, Object> mapBean = new HashMap<>();
    public void putBean(String chiave, Object bean) {
        this.mapBean.put(chiave, bean);
    }
    public Object getBean(String chiave) {
        return this.mapBean.get(chiave);
    }
}
