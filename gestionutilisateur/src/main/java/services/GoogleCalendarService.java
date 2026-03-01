package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import entities.Recrutement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "VOS Recruitment Management";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void syncRecrutements(List<Recrutement> recrutements) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        for (Recrutement r : recrutements) {
            // Créer un événement pour chaque recrutement
            Event event = new Event()
                    .setSummary("Recrutement #" + r.getId_recrutement() + " - " + r.getDecision_finale())
                    .setDescription("Décision finale de recrutement pour l'entretien #" + r.getId_entretien());

            // La date de décision (SQL Date -> java.util.Date -> DateTime)
            java.util.Date decisionDate = new java.util.Date(r.getDate_decision().getTime());
            DateTime startDateTime = new DateTime(decisionDate);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Africa/Tunis");
            event.setStart(start);

            // Fin de l'événement (1 heure plus tard par défaut)
            java.util.Date endDate = new java.util.Date(decisionDate.getTime() + 3600000);
            DateTime endDateTime = new DateTime(endDate);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Africa/Tunis");
            event.setEnd(end);

            // Insérer l'événement dans le calendrier principal
            service.events().insert("primary", event).execute();
        }
    }
}
