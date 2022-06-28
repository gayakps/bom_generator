package gaya.pe.kr.type;

import java.util.Arrays;
import java.util.List;

public enum KeyType {

    NONE(null),
    LICENSE(Arrays.asList("name","url")),
    DEPENDENCY(Arrays.asList("groupId","artifactId","version"));

    final List<String> tag;

    KeyType(List<String> tag) {
        this.tag = tag;
    }

    public List<String> getTag() {
        return tag;
    }
}
