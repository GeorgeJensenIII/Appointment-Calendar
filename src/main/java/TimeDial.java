/**
 * Created by gwjense on 1/3/18.
 */

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.InputEvent;
import javafx.util.StringConverter;

public class TimeDial extends Spinner<LocalTime> {

    enum State {

        HOURS {
            @Override
            LocalTime increment(LocalTime time, int ticks) {
                return time.plusHours(ticks);
            }
            @Override
            void select(TimeDial spinner) {
                int index = spinner.getEditor().getText().indexOf(':');
                spinner.getEditor().selectRange(0, index);
            }
        },
        MINUTES {
            @Override
            LocalTime increment(LocalTime time, int ticks) {
                return time.plusMinutes(ticks);
            }
            @Override
            void select(TimeDial spinner) {
                int hrIndex = spinner.getEditor().getText().indexOf(':');
                int minIndex = spinner.getEditor().getText().indexOf(':', hrIndex + 1);
                spinner.getEditor().selectRange(hrIndex+1, minIndex);
            }
        };
        
        abstract LocalTime increment(LocalTime time, int ticks);
        abstract void select(TimeDial spinner);
        LocalTime decrement(LocalTime time, int ticks) {
            return increment(time, -ticks);
        }
    }
    

    private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.HOURS) ;

    public ObjectProperty<State> stateProperty() {
        return state;
    }

    public final State getState() {
        return stateProperty().get();
    }

    public final void setState(State state) {
        stateProperty().set(state);
    }


    public TimeDial(LocalTime time) {
        setEditable(true);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        StringConverter<LocalTime> localTimeConverter = new StringConverter<LocalTime>() {

            @Override
            public String toString(LocalTime time) {
                return formatter.format(time);
            }

            @Override
            public LocalTime fromString(String string) {
                String[] characters = string.split(":");
                int hours = getValueOf(characters, 0);
                int minutes = getValueOf(characters, 1) ;
                int seconds = getValueOf(characters, 2);
                int totalSeconds = (hours * 60 + minutes) * 60 + seconds ;
                return LocalTime.of((totalSeconds / 3600) % 24, (totalSeconds / 60) % 60, seconds % 60);
            }

            private int getValueOf(String[] characters, int index) {
                if (characters.length <= index || characters[index].isEmpty()) {
                    return 0 ;
                }
                return Integer.parseInt(characters[index]);
            }

        };


        TextFormatter<LocalTime> textFormatter = new TextFormatter<LocalTime>(localTimeConverter, time, c -> {
            String newText = c.getControlNewText();
            if (newText.matches("[0-9]{0,2}:[0-9]{0,2}:[0-0]{0,2}")) {
                return c ;
            }
            return null ;
        });


        SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<LocalTime>() {


            {

                setConverter(localTimeConverter);
                setValue(time);
            }

            @Override
            public void decrement(int ticks) {
                setValue(state.get().decrement(getValue(), ticks));
                state.get().select(TimeDial.this);
            }

            @Override
            public void increment(int ticks) {
                setValue(state.get().increment(getValue(), ticks));
                state.get().select(TimeDial.this);
            }

        };

        this.setValueFactory(valueFactory);
        this.getEditor().setTextFormatter(textFormatter);

        // Update the state when the user input.

        this.getEditor().addEventHandler(InputEvent.ANY, e -> {
            int caretPos = this.getEditor().getCaretPosition();
            int hrIndex = this.getEditor().getText().indexOf(':');
            int minIndex = this.getEditor().getText().indexOf(':', hrIndex + 1);
            if (caretPos <= hrIndex) {
                state.set( State.HOURS );
            } else if (caretPos <= minIndex) {
                state.set( State.MINUTES );
            } else {
                //state.set( State.SECONDS );
                state.set( State.MINUTES );

            }
        });

        // When the state changes, select the new portion:
        state.addListener((obs, oldState, newState) -> newState.select(this));

    }

    public TimeDial() {
        this(LocalTime.of(12,00,00));
    }
}

