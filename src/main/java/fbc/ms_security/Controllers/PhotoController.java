/*
 * Controlador para gestionar las fotos en el sistema.
 * Permite subir, ver, actualizar y listar fotos.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de fotos.
 * @Autowired: Inyección automática del repositorio de fotos.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Photo;
import fbc.ms_security.Repositories.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "${frontend.url}")
@RestController
@RequestMapping("/api/photo")
public class PhotoController {
    @Autowired // Inyección automática del repositorio de fotos
    private PhotoRepository thePhotoRepository;

    /**
     * Obtiene todas las fotos registradas.
     * GET /api/photo
     */
    @GetMapping("")
    public List<Photo> find() {
        return this.thePhotoRepository.findAll();
    }

    /**
     * Busca una foto por su ID.
     * GET /api/photo/{id}
     */
    @GetMapping("{id}")
    public Photo findById(@PathVariable String id) {
        return this.thePhotoRepository.findById(id).orElse(null);
    }

    /**
     * Sube una nueva foto.
     * POST /api/photo/upload
     * @RequestParam("file"): Recibe el archivo como parámetro.
     */
    @PostMapping("/upload")
    public Photo upload(@RequestParam("file") MultipartFile file) throws Exception {
        Photo photo = new Photo();
        photo.setData(file.getBytes()); // Guarda los bytes de la imagen
        photo.setContentType(file.getContentType()); // Guarda el tipo MIME
        return this.thePhotoRepository.save(photo);
    }

    /**
     * Devuelve la imagen en formato binario para mostrarla.
     * GET /api/photo/view/{id}
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> view(@PathVariable String id) {
        Photo photo = this.thePhotoRepository.findById(id).orElse(null);
        if (photo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(photo.getData());
    }

    /**
     * Actualiza una foto existente.
     * PUT /api/photo/{id}
     */
    @PutMapping("{id}")
    public Photo update(@PathVariable String id, @RequestParam("file")  MultipartFile file) throws Exception {
        Photo actualPhoto = this.thePhotoRepository.findById(id).orElse(null);
        if (actualPhoto != null) {
            actualPhoto.setData(file.getBytes());
            actualPhoto.setContentType(file.getContentType());
            return this.thePhotoRepository.save(actualPhoto);
        
        } else { return null; }
    }

    /**
     * Elimina una foto por su ID.
     * DELETE /api/photo/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Photo thePhoto = this.thePhotoRepository.findById(id).orElse(null);
        if (thePhoto != null) {
            this.thePhotoRepository.delete(thePhoto);
        }
    }
}