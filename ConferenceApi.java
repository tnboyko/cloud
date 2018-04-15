package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.googlecode.objectify.Key;

/**
 * Defines conference APIs.
 */
@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = {
        Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID }, description = "API for the Conference Central Backend application.")
public class ConferenceApi {
	
	private static final Logger LOG = Logger.getLogger(ConferenceApi.class.getName());

    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @param profileForm
     *            A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException
     *             when the User object is null.
     */

    // Declare this method as a method available externally through Endpoints
    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    // The request that invokes this method should provide data that
    // conforms to the fields defined in ProfileForm

    // TODO 1 Pass the ProfileForm parameter
    
    private static Profile getProfileFromUser(User user, String userId) {
        // First fetch it from the datastore.
        Profile profile = ofy().load().key(
                Key.create(Profile.class, userId)).now();
        if (profile == null) {
            // Create a new Profile if not exist.
            String email = user.getEmail();
            profile = new Profile(userId,
                    extractDefaultDisplayNameFromEmail(email), email, TeeShirtSize.NOT_SPECIFIED);
        }
        return profile;
    }
    
    private static String getUserId(User user) {
        String userId = user.getUserId();
        if (userId == null) {
            LOG.info("userId is null, so trying to obtain it from the datastore.");
            AppEngineUser appEngineUser = new AppEngineUser(user);
            ofy().save().entity(appEngineUser).now();
            // Begin new session for not using session cache.
            Objectify objectify = ofy().factory().begin();
            AppEngineUser savedUser = objectify.load().key(appEngineUser.getKey()).now();
            userId = savedUser.getUser().getUserId();
            LOG.info("Obtained the userId: " + userId);
        }
        return userId;
    }
    
    // TODO 2 Pass the User parameter
    public Profile saveProfile(final User user, ProfileForm profileForm) throws UnauthorizedException {

        String userId = null;
        String mainEmail = null;
        String displayName = "Your name will go here";
        TeeShirtSize teeShirtSize = TeeShirtSize.NOT_SPECIFIED;

        // TODO 2
        // If the user is not logged in, throw an UnauthorizedException
        if (user == null){ 
            throw new UnauthorizedException("You are not logged in"); 
           } 
        /**
         * Returns a Profile object associated with the given user object. The cloud endpoints system
         * automatically inject the User object.
         *
         * @param user A User object injected by the cloud endpoints.
         * @return Profile object.
         * @throws UnauthorizedException when the User object is null.
         */
        @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
        public Profile getProfile(final User user) throws UnauthorizedException {
            if (user == null) {
                throw new UnauthorizedException("Authorization required");
            }
            return ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
        }

        /**
         * Creates or updates a Profile object associated with the given user object.
         *
         * @param user A User object injected by the cloud endpoints.
         * @param profileForm A ProfileForm object sent from the client form.
         * @return Profile object just created.
         * @throws UnauthorizedException when the User object is null.
         */
        @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
        public Profile saveProfile(final User user, final ProfileForm profileForm)
                throws UnauthorizedException {
            if (user == null) {
                throw new UnauthorizedException("Authorization required");
            }
            String displayName = profileForm.getDisplayName();
            TeeShirtSize teeShirtSize = profileForm.getTeeShirtSize();

            Profile profile = ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
            if (profile == null) {
                // Populate displayName and teeShirtSize with the default values if null.
                if (displayName == null) {
                    displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
                }
                if (teeShirtSize == null) {
                    teeShirtSize = TeeShirtSize.NOT_SPECIFIED;
                }
                profile = new Profile(getUserId(user), displayName, user.getEmail(), teeShirtSize);
            } else {
                profile.update(displayName, teeShirtSize);
            }
            ofy().save().entity(profile).now();
            return profile;
        }

        // TODO 2
        // Get the userId and mainEmail
        String mainEmail = user.getEmail(); 
        String userId = getUserId(user); 
 
        String displayName = profileForm.getDisplayName(); 
        TeeShirtSize teeShirtSize = profileForm.getTeeShirtSize(); 
         
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now(); 
        if(profile == null){ 
          if(displayName == null){ 
              displayName = extractDefaultDisplayNameFromEmail(user.getEmail()); 
             } 
          if(teeShirtSize == null){ 
              teeShirtSize = teeShirtSize.NOT_SPECIFIED; 
             } 
          profile = new Profile(userId, displayName, mainEmail, teeShirtSize); 
   
        } else{ 
         profile.update(displayName, teeShirtSize); 
          
        } 

        // TODO 2
        // If the displayName is null, set it to default value based on the user's email
        // by calling extractDefaultDisplayNameFromEmail(...)
        @ApiMethod(name = "createConference", path = "conference", httpMethod = HttpMethod.POST)
        public Conference createConference(final User user, final ConferenceForm conferenceForm)
            throws UnauthorizedException {
            if (user == null) {
                throw new UnauthorizedException("Authorization required");
            }
            // Allocate Id first, in order to make the transaction idempotent.
            Key<Profile> profileKey = Key.create(Profile.class, getUserId(user));
            final Key<Conference> conferenceKey = factory().allocateId(profileKey, Conference.class);
            final long conferenceId = conferenceKey.getId();
            final Queue queue = QueueFactory.getDefaultQueue();
            final String userId = getUserId(user);
            // Start a transaction.
            Conference conference = ofy().transact(new Work<Conference>() {
                @Override
                public Conference run() {
                    // Fetch user's Profile.
                    Profile profile = getProfileFromUser(user, userId);
                    Conference conference = new Conference(conferenceId, userId, conferenceForm);
                    // Save Conference and Profile.
                    ofy().save().entities(conference, profile).now();
                    queue.add(ofy().getTransaction(),
                            TaskOptions.Builder.withUrl("/tasks/send_confirmation_email")
                            .param("email", profile.getMainEmail())
                            .param("conferenceInfo", conference.toString()));
                    return conference;
                }
            });
            return conference;
        }
        // Create a new Profile entity from the
        // userId, displayName, mainEmail and teeShirtSize
        Profile profile = new Profile(userId, displayName, mainEmail, teeShirtSize);

        // TODO 3 (In Lesson 3)
        // Save the Profile entity in the datastore
        ofy().save().entity(profile).now();
        // Return the profile
        return profile;
    }

    /**
     * Returns a Profile object associated with the given user object. The cloud
     * endpoints system automatically inject the User object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException
     *             when the User object is null.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // TODO
        Key key = Key.create(Profile.class, getUserId(user));
        // load the Profile Entity
        Profile profile = (Profile) ofy().load().key(key).now(); // TODO load the Profile entity 
        return profile; 
    }
}
