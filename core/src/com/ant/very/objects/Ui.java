package com.ant.very.objects;

import com.ant.very.ActionResolver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ExtendViewport;


public class Ui {
    private static final String TAG = "Ui";
    private Stage stage;
    private Skin skin;

    private TextField inputTextField;
    private TextArea botResponseTextArea;
    private MicButton micButton;
    private Action shakeAction;
    private ActionResolver actionResolver;
    private TextureAtlas uiTextureAtlas;

//    This exists so that tapping the mic button while listening may stop this action.
    private boolean isCurrentlyRecognizingSpeech = false;

    public Ui(ActionResolver ar) {
        actionResolver = ar;

//        Create the stage
        stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        uiTextureAtlas = new TextureAtlas(Gdx.files.internal("Scene2D/uitextareas.atlas"));
        skin = new Skin(Gdx.files.internal("Scene2D/uiskin.json"));
        skin.addRegions(uiTextureAtlas);

//        Create the actors
        botResponseTextArea = new TextArea("\n Tell me something!...", skin);
        inputTextField = new TextField("   ...", skin);
        micButton = new MicButton();
        setupActors();

        Group group = new Group();
        group.addActor(inputTextField);
        group.addActor(botResponseTextArea);
        group.addActor(micButton);
        stage.addActor(group);
    }

//    Get the stage for input processing over in VRAS:
    public Stage getStage() {
        return stage;
    }

//    Set all the sizes & other parameters for UI elements:
    private void setupActors() {
        float textBoxWidth = Gdx.graphics.getWidth();
        float textBoxHeight = 120;
        float offset = Gdx.graphics.getWidth() - textBoxWidth;

//        botResponseTextArea.setStyle(new TextField.TextFieldStyle())
        botResponseTextArea.setWidth(textBoxWidth);
        botResponseTextArea.setHeight(textBoxHeight);
        botResponseTextArea.setPrefRows(2);
        botResponseTextArea.setX(offset - offset / 2.0f);
        botResponseTextArea.setY(Gdx.graphics.getHeight() - textBoxHeight);
        botResponseTextArea.setDisabled(true);

        inputTextField.setWidth(textBoxWidth);
        inputTextField.setHeight(textBoxHeight);
        inputTextField.setX(offset - offset / 2.0f);
        inputTextField.setY(Gdx.graphics.getHeight() - textBoxHeight * 2);

        inputTextField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\r' || c == '\n') {
                    String sentence = textField.getText();
                    if (sentence != null) {
                        // Send the contents of the input textfield to the bot:
                        actionResolver.handleBotQuestion(sentence);
                        textField.setText(" ");
                    }
                }
            }
        });

        micButton.setTouchable(Touchable.enabled);
        micButton.setPosition(inputTextField.getX() + inputTextField.getWidth() - 15
                - micButton.getWidth(), inputTextField.getY() - 10);
    }

    public void setCurrentlyRecognizingSpeech(boolean is) {
        isCurrentlyRecognizingSpeech = is;
    }

    public void disposeUi() {
        stage.dispose();
        skin.dispose();
    }

    public void renderUI() {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private class MicButton extends Actor {
        Texture texture = new Texture(Gdx.files.internal("Scene2D/mic.png"));

        protected MicButton() {
            setBounds(getX(), getY(), texture.getWidth()/1.5f, texture.getHeight()/1.5f);
            setOrigin(texture.getWidth()/3.0f, texture.getHeight()/3.0f);

            addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (!isCurrentlyRecognizingSpeech) {
                        actionResolver.recognizeSpeech();
                        setCurrentlyRecognizingSpeech(true);
                    }
                    else {
                        actionResolver.stopListeningForSpeech();
                        setCurrentlyRecognizingSpeech(false);
                    }
                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(),
                    getHeight(), getScaleX(), getScaleY(), getRotation(), 0, 0,
                    texture.getWidth(), texture.getHeight(), false, false);
        }
    }

    // Methods accessed from MyListener:
    public void setInputTextFieldText(String text) {
        this.inputTextField.setText(" " + text);
    }

    public void showToast(String message) { actionResolver.showToast(message, 5000);}

    public void setBotResponseTextAreaText(String text) {
        this.botResponseTextArea.setText(" " + text);
    }

    public void actionPulseMicButton() {
        shakeAction = Actions.forever(
                Actions.sequence(
                        Actions.scaleTo(1.05f, 1.05f, 0.2f),
                        Actions.scaleTo(1.0f, 1.0f, 0.2f),
                        Actions.scaleTo(1.05f, 1.05f, 0.2f),
                        Actions.scaleTo(1.0f, 1.0f, 0.2f),
                        Actions.delay(0.5f)
                ));
        if (micButton.getActions().size == 0) {
            micButton.addAction(shakeAction);
            micButton.act(Gdx.graphics.getDeltaTime());
        }
    }

//    Kinda collides with the button pulse.
//    public void actionScaleMicButton(float value) {
//        micButton.setScale(value);
//        micButton.act(Gdx.graphics.getDeltaTime());
//    }

    public void stopMicButtonPulse() {
        micButton.clearActions();
        micButton.setScale(1.0f, 1.0f);
    }
}
