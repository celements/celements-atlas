package org.opencelements.atlas.adapters.driving.controller;

import java.util.List;

import org.opencelements.atlas.adapters.driving.mapper.DrivingMapper;
import org.opencelements.atlas.adapters.driving.model.DocumentDto;
import org.opencelements.atlas.application.model.AtlasDocument;
import org.opencelements.atlas.application.model.AtlasObject;
import org.opencelements.atlas.application.model.exceptions.DocumentCreationException;
import org.opencelements.atlas.application.model.exceptions.DocumentNotFoundException;
import org.opencelements.atlas.application.model.exceptions.DocumentUpdateException;
import org.opencelements.atlas.application.ports.driving.DocumentCreateCommand;
import org.opencelements.atlas.application.ports.driving.DocumentLoadCommand;
import org.opencelements.atlas.application.ports.driving.DocumentUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

  private final DocumentLoadCommand loadCommand;
  private final DocumentCreateCommand createCommand;
  private final DocumentUpdateCommand updateCommand;
  private final DrivingMapper mapper;

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

  @Inject
  public DocumentController(
      DocumentLoadCommand loadCommand,
      DocumentCreateCommand createCommand,
      DocumentUpdateCommand updateCommand,
      DrivingMapper mapper) {
    this.loadCommand = loadCommand;
    this.createCommand = createCommand;
    this.updateCommand = updateCommand;
    this.mapper = mapper;
  }

  @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public String create(@RequestBody List<org.bson.Document> objectData)
      throws DocumentCreationException {
    return createCommand.create(AtlasDocument.builder()
        .objects(convert(objectData))
        .build());
  }

  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public void update(
      @PathVariable String id,
      @RequestBody List<org.bson.Document> objectData)
      throws DocumentNotFoundException, DocumentUpdateException {
    updateCommand.update(AtlasDocument.builder()
        .id(id)
        .objects(convert(objectData))
        .build());
  }

  private List<AtlasObject> convert(List<org.bson.Document> objectData) {
    return objectData.stream()
        .map(obj -> AtlasObject.builder().data(obj).build())
        .toList();
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public DocumentDto get(@PathVariable String id) throws DocumentNotFoundException {
    LOGGER.debug("Getting document with id: {}", id);
    var doc = loadCommand.load(id);
    return mapper.toDocumentDto(doc);
  }

  @ExceptionHandler(DocumentNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String documentNotFoundHandler(DocumentNotFoundException ex) {
    return ex.getMessage();
  }

  @ExceptionHandler(DocumentCreationException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  String documentNotCreatedHandler(DocumentCreationException ex) {
    return ex.getMessage();
  }

  @ExceptionHandler(DocumentUpdateException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  String documentNotUpdatedHandler(DocumentUpdateException ex) {
    return ex.getMessage();
  }
}
