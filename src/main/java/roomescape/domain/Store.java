package roomescape.domain;

import lombok.Getter;
import roomescape.global.exception.store.InvalidStoreException;

@Getter
public class Store {

    private final Long id;
    private final String name;

    private Store(Long id, String name) {
        validateName(name);
        this.id = id;
        this.name = name;
    }

    public static Store createNew(String name) {
        return new Store(null, name);
    }

    public static Store from(Long id, String name) {
        return new Store(id, name);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidStoreException("매장 이름은 비어있을 수 없습니다.");
        }
    }
}
