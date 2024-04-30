package org.opencelements.atlas.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "document")
public class Document {

    @Id
    private final String id;
    @DBRef
    private final List<DataObject> objects;

}
