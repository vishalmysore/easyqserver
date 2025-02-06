package regression;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vishalmysore.data.Story;

public class JsonParsing {

    public static void main(String[] args) {
        String json ="{\n" +
                "  \"title\": \"The Whispering Shadows\",\n" +
                "  \"storyText\": \"In the small town of Eldridge Hollow, the sun set behind the hills, casting long shadows across the cobbled streets. The townsfolk often whispered about the old Whitmore house at the end of Maple Lane. Abandoned for decades, it stood like a decaying monument to the past, its windows dark and its doors creaking in the wind. Children dared each other to knock, but no one ever did.\\n\\nOne autumn evening, a curious girl named Lily decided to explore the infamous house. With her flashlight in hand and her heart racing with a mix of fear and excitement, she made her way toward the towering structure. The air was thick with the scent of damp leaves, and the night was silent except for the crunch of her footsteps.\\n\\nAs she approached the front door, the wind picked up, sending a chill through her bones. The door, surprisingly, swung open with a low groan, revealing a dimly lit foyer. Dust motes danced in the beam of her flashlight as she stepped inside. The air felt heavy, as if the house was holding its breath, waiting for her to make the next move.\\n\\nShe wandered through the rooms, each one filled with remnants of the past: a broken rocking chair, a dusty piano, and faded photographs of a family that once lived there. The further she ventured, the more she felt an unseen presence. Shadows flickered at the corners of her vision, but when she turned to look, there was nothing.\\n\\nIn the heart of the house, she discovered a grand staircase. It spiraled upward, its wooden steps worn and creaky. Driven by a mix of bravery and curiosity, Lily ascended the stairs. As she reached the top, she noticed a door slightly ajar at the end of the hallway. A soft whisper floated through the crack, calling her name. It was faint, almost like a breeze brushing against her ear, yet it was unmistakably human.\\n\\n\\\"Lily... come closer...\\\" the voice beckoned.\\n\\nHer heart raced as she approached the door. She pushed it open, revealing a small room bathed in moonlight. In the center stood an antique mirror, its surface clouded and tarnished. The whispers grew louder, swirling around her like a storm.\\n\\n\\\"Look into the mirror, Lily... we are waiting for you...\\\" \\n\\nWith trembling hands, she reached for the mirror's edge. As she looked into its depths, the reflection began to change. Instead of her own face, she saw the silhouettes of figures, their features obscured but their eyes wide and pleading. They reached out to her, desperation etched in their ghostly forms. \\n\\n\\\"Help us... find peace...\\\" they whispered in unison.\\n\\nFear surged through her, yet she felt an inexplicable urge to help. \\\"How?\\\" she managed to ask, her voice barely above a whisper. \\n\\n\\\"Uncover the truth... free us from this place...\\\" \\n\\nSuddenly, the whispers ceased, and the room fell silent. The mirror returned to its regular reflection, showing only Lily’s bewildered face. Heart pounding, she knew she had to leave but also knew she couldn’t ignore what she had witnessed.\\n\\nAs she retraced her steps down the staircase, the house felt different, almost alive. The whispers had faded, but their message lingered in her mind. Lily promised herself she would return, not just to confront the shadows of the Whitmore house, but to help free the souls trapped within its walls.\"\n" +
                "}";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Story story = objectMapper.readValue(json, Story.class);
            System.out.println(story);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
