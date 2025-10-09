package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Photo;
import fbc.ms_security.Repositories.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photo")
public class PhotoController {
    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);
    private final PhotoRepository thePhotoRepository;

    public PhotoController(PhotoRepository thePhotoRepository) {
        this.thePhotoRepository = thePhotoRepository;
        logger.info("PhotoController inicializado correctamente");
    }

    /**
     * Obtiene todas las fotos registradas.
     * GET /api/photo
     */
    @GetMapping("")
    public ResponseEntity<List<Photo>> find() {
        logger.info("Solicitando lista de todas las fotos");
        try {
            List<Photo> photos = this.thePhotoRepository.findAll();
            if (photos.isEmpty()) {
                logger.warn("No se encontraron fotos en el sistema");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Se encontraron {} fotos", photos.size());
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            logger.error("Error al obtener lista de fotos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca una foto por su ID.
     * GET /api/photo/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Photo> findById(@PathVariable String id) {
        logger.info("Buscando foto con ID: {}", id);
        try {
            Photo thePhoto = this.thePhotoRepository.findById(id).orElse(null);
            if (thePhoto == null) {
                logger.warn("Foto no encontrada con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Foto encontrada con ID: {}", id);
            logger.debug("Tipo de contenido: {}", thePhoto.getContentType());
            return ResponseEntity.ok(thePhoto);
        } catch (Exception e) {
            logger.error("Error al buscar foto con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Devuelve la imagen en formato binario para mostrarla.
     * GET /api/photo/view/{id}
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> view(@PathVariable String id) {
        logger.info("Solicitando visualización de foto con ID: {}", id);
        try {
            Photo photo = this.thePhotoRepository.findById(id).orElse(null);
            if (photo == null) {
                logger.warn("Foto no encontrada para visualizar con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Retornando imagen con tipo de contenido: {}", photo.getContentType());
            logger.debug("Tamaño de imagen: {} bytes", photo.getData().length);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(photo.getContentType()))
                    .body(photo.getData());
        } catch (Exception e) {
            logger.error("Error al visualizar foto con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sube una nueva foto.
     * POST /api/photo/upload
     * @RequestParam("file"): Recibe el archivo como parámetro.
     */
    @PostMapping("/upload")
    public ResponseEntity<Object> upload(@RequestParam("file") MultipartFile file) {
        logger.info("Intentando subir nueva foto");
        logger.debug("Nombre del archivo: {}, Tipo: {}, Tamaño: {} bytes",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        try {
            if (file.isEmpty()) {
                logger.warn("Intento de subir archivo vacío");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "El archivo está vacío",
                                "status", String.valueOf(HttpStatus.BAD_REQUEST.value())
                        ));
            }

            Photo photo = new Photo();
            photo.setData(file.getBytes());
            photo.setContentType(file.getContentType());
            Photo savedPhoto = this.thePhotoRepository.save(photo);

            logger.info("Foto subida exitosamente con ID: {}", savedPhoto.get_id());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedPhoto);
        } catch (Exception e) {
            logger.error("Error al subir foto: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al subir foto: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Actualiza una foto existente.
     * PUT /api/photo/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        logger.info("Intentando actualizar foto con ID: {}", id);
        logger.debug("Nuevo archivo - Nombre: {}, Tipo: {}, Tamaño: {} bytes",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        try {
            Photo actualPhoto = this.thePhotoRepository.findById(id).orElse(null);
            if (actualPhoto != null) {
                logger.debug("Foto encontrada, actualizando datos");
                actualPhoto.setData(file.getBytes());
                actualPhoto.setContentType(file.getContentType());
                Photo updatedPhoto = this.thePhotoRepository.save(actualPhoto);
                logger.info("Foto actualizada exitosamente con ID: {}", id);
                return ResponseEntity.ok(updatedPhoto);
            } else {
                logger.warn("Foto no encontrada para actualizar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Foto no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al actualizar foto con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al actualizar foto: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina una foto por su ID.
     * DELETE /api/photo/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar foto con ID: {}", id);

        try {
            Photo thePhoto = this.thePhotoRepository.findById(id).orElse(null);
            if (thePhoto != null) {
                logger.debug("Foto encontrada, procediendo a eliminar");
                this.thePhotoRepository.delete(thePhoto);
                logger.info("Foto eliminada exitosamente con ID: {}", id);
                return ResponseEntity.ok(Map.of("message", "Foto eliminada correctamente"));
            } else {
                logger.warn("Foto no encontrada para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Foto no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar foto con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar foto: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}