package jp.broadcom.tanzu.mhoshi.social.collector.shared;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OffsetStore {

    @Id
    CollectorType collector;

    String pointer;

    public OffsetStore() {
    }

    public OffsetStore(CollectorType collector, String pointer) {
        this.collector = collector;
        this.pointer = pointer;
    }

    public CollectorType getCollector() {
        return collector;
    }

    public void setCollector(CollectorType collector) {
        this.collector = collector;
    }

    public String getPointer() {
        return pointer;
    }

    public void setPointer(String offset) {
        this.pointer = offset;
    }

}