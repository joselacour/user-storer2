package controller;

import java.time.Instant;

import dto.ErrorResponseDTO;
import dto.LoginRequestDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.AuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Uni<Response> login(@Valid LoginRequestDTO input) {
        return authService.login(input.getEmail(), input.getPassword())
            .map(dto -> Response.ok(dto).build())
            .onFailure().recoverWithItem(throwable -> {
                ErrorResponseDTO err = new ErrorResponseDTO(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Invalid credentials",
                    throwable.getMessage(),
                    Instant.now(),
                    null
                );
                return Response.status(Response.Status.UNAUTHORIZED).entity(err).build();
            });
    }
}