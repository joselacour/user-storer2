package controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import dto.ErrorOutputDTO;
import dto.UserCreateInputDTO;
import dto.UserOutputDTO;
import io.smallrye.mutiny.Uni;
import model.User;
import service.UserService;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
// Podriamos @RequestScoped si queremos que se cree una nueva instancia del controller por cada peticion
public class UserController {

	@Inject
	UserService userService;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "201", description = "User created successfully")
    @APIResponse(responseCode = "400", description = "Bad request")
    public Uni<Response> create(@Valid UserCreateInputDTO input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setPassword(input.getPassword());
        user.setEmail(input.getEmail());

        return userService.create(user)
            .map(savedUser -> {
                UserOutputDTO dto = new UserOutputDTO(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getLastLogin(),
                    savedUser.getCreated(),
                    savedUser.getModified()
                );
                return Response.status(Response.Status.CREATED).entity(dto).build();
            })
            .onFailure().recoverWithItem(throwable -> {
                ErrorOutputDTO err = new ErrorOutputDTO(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "Error creating user",
                    throwable.getMessage(),
                    Instant.now(),
                    null // de momento no devolvemos el user ya que no manejamos sesion
                );
                return Response.status(Response.Status.BAD_REQUEST)
                            .entity(err)
                            .build();
            });
    }

    @GET
    @Path("/")
    @APIResponse(responseCode = "200", description = "Users founds successfully")
    @APIResponse(responseCode = "500", description = "Internal server error")
    // A manera de debug estoy segira devolviendo user y no el DTO, para poder ver todos los campos
    public Uni<Response> findAll() {
        return userService.findAll()
            .map(users -> Response.ok(users).build())
            .onFailure().recoverWithItem(throwable -> {
                ErrorOutputDTO err = new ErrorOutputDTO(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Error retrieving users",
                    throwable.getMessage(),
                    Instant.now(),
                    null // de momento no devolvemos el user ya que no manejamos sesion
                );
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(err)
                            .build();

            });
    }


	@GET
	@Path("/{id}")
    @APIResponse(responseCode = "200", description = "User found successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
	public Uni<Response> findById(@PathParam("id") String id) {
		return userService.findById(id)
               .map(user -> {
                   if (user == null) {
                       return Response.status(Response.Status.NOT_FOUND)
                               .entity("User with id " + id + " not found")
                               .build();
                   }
                    UserOutputDTO dto = new UserOutputDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getLastLogin(),
                    user.getCreated(),
                    user.getModified()
                    );
                   return Response.ok(dto).build();
               })
               .onFailure().recoverWithItem(throwable -> {
                   ErrorOutputDTO err = new ErrorOutputDTO(
                       Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                       "Error retrieving user",
                       throwable.getMessage(),
                       Instant.now(),
                       null // de momento no devolvemos el user ya que no manejamos sesion
                   );
                   return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity(err)
                               .build();
               });
	}

    @GET
    @Path("/searchby/email")
    @APIResponse(responseCode = "200", description = "User found successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public Uni<Response> findByEmail(@QueryParam("email") String email) {
        return userService.findByEmail(email)
            .map(user -> {
                if (user == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity("User with email " + email + " not found")
                        .build();
                }
                UserOutputDTO dto = new UserOutputDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getLastLogin(),
                    user.getCreated(),
                    user.getModified()
                );
                return Response.ok(dto).build();
            })
            .onFailure().recoverWithItem(throwable -> {
                ErrorOutputDTO err = new ErrorOutputDTO(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Error retrieving user by email",
                    throwable.getMessage(),
                    Instant.now(),
                    null // de momento no devolvemos el user ya que no manejamos sesion
                );
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(err)
                            .build();
            });
    }

    @GET
    @Path("/searchby/username")
    @APIResponse(responseCode = "200", description = "User found successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public Uni<Response> findByUsername(@QueryParam("username") String username) {
        return userService.findByUsername(username)
            .map(user -> {
                if (user == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity("User with username " + username + " not found")
                        .build();
                }
                UserOutputDTO dto = new UserOutputDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getLastLogin(),
                    user.getCreated(),
                    user.getModified()
                );
                return Response.ok(dto).build();
            })
            .onFailure().recoverWithItem(throwable -> {
                ErrorOutputDTO err = new ErrorOutputDTO(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Error retrieving user by username",
                    throwable.getMessage(),
                    Instant.now(),
                    null // de momento no devolvemos el user ya que no manejamos sesion
                );
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(err)
                            .build();
            });
    }

    @DELETE
    @Path("/{id}")
    @APIResponse(responseCode = "204", description = "User deleted successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public Uni<Response> delete(@PathParam("id") String id) {
        return userService.delete(id)
            .map(v -> Response.noContent().build())
            .onFailure().recoverWithItem(throwable -> {
                ErrorOutputDTO err = new ErrorOutputDTO(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    "Error deleting user",
                    throwable.getMessage(),
                    Instant.now(),
                    null // de momento no devolvemos el user ya que no manejamos sesion
                );
                return Response.status(Response.Status.NOT_FOUND)
                            .entity(err)
                            .build();
            });
    }

}
