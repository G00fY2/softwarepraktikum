package de.rwth.dbis.ugnm.resource;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.WebApplicationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.rwth.dbis.ugnm.entity.Achievement;
import de.rwth.dbis.ugnm.service.AchievementService;

@Path("/achievements")
@Component
@Scope("request")
public class AchievementsResource {
        
        
        

        @Autowired
        AchievementService achievementService;

        @Context UriInfo uriInfo;

        
        //Get all Achievements
        @GET
        @Produces("application/json")
        public JSONObject getAchievements() {

                List<Achievement> achievements = achievementService.getAll();
                Iterator<Achievement> ait = achievements.iterator();
                
                Vector<String> vAchievements = new Vector<String>();    
                
                while(ait.hasNext()){
                        Achievement a = ait.next();
                        String aUri = uriInfo.getAbsolutePath().toASCIIString() + "/" + a.getId();
                        vAchievements.add(aUri);
                }

                try {
                        JSONObject j = new JSONObject();
                        j.append("achievements",vAchievements);
                        return j;
                } catch (JSONException e) {
                        throw new WebApplicationException(500);
                }
        }
        
        
        //This creates a new Achievement
        @PUT
    @Consumes("application/json")
    public Response createAchievement(JSONObject o) throws JSONException {
                //Create a new achievement..
                Achievement achievement = parseAchievementJsonFile(o);
                //call this method to make sure no double entries are inserted
                return addIfDoesNotExist(achievement);
    }
        
        private Response addIfDoesNotExist(Achievement achievement) {
                if(achievementService.findAchievement(achievement) == null) {
                        achievementService.save(achievement);
                        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
                        String relativePath = ""+achievement.getId(); //Not very nice but it works
                        URI achievementUri = ub.path(relativePath).build();
                        return Response.created(achievementUri).build();
                }
                else{
                        throw new WebApplicationException(409);
                }
        }
        
        //parse the JSON File for the attributes
        private Achievement parseAchievementJsonFile(JSONObject o){
            try {
            	    int id = o.getInt("id");
                    String description = o.getString("description");
                    String name = o.getString("name");
                    String url = o.getString("url");
                    Achievement achievement = new Achievement();
                    achievement.setId(id);
                    achievement.setName(name);
                    achievement.setDescription(description);
                    achievement.setUrl(url);
                    return achievement;
            } catch (JSONException e) {
                    throw new WebApplicationException(409);
            }
                
        }
}