package com.volt.utils.render;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public final class AnimationUtils {

    private static final Map<String, Animation> animations = new ConcurrentHashMap<>();

    public static float animate(String id, float target, long duration, EasingFunction easing) {
        Animation animation = animations.get(id);

        if (animation == null) {
            animation = new Animation(0, target, duration, easing);
            animations.put(id, animation);
        } else if (animation.targetValue != target) {
            animation.setTarget(target);
        }

        return animation.update();
    }

    public static float animate(String id, float target, long duration) {
        return animate(id, target, duration, Easing.EASE_OUT_CUBIC);
    }

    public static boolean isAnimationCompleted(String id) {
        Animation animation = animations.get(id);
        return animation == null || animation.isCompleted();
    }

    public static void removeAnimation(String id) {
        animations.remove(id);
    }

    public static void clearAnimations() {
        animations.clear();
    }

    public static float pulse(String id, float speed) {
        float time = System.currentTimeMillis() * 0.001f * speed;
        return 0.5f + 0.5f * (float) Math.sin(time);
    }

    public static float wave(String id, float speed, float amplitude, float offset) {
        float time = System.currentTimeMillis() * 0.001f * speed + offset;
        return amplitude * (float) Math.sin(time);
    }

    @FunctionalInterface
    public interface EasingFunction {
        float apply(float t);
    }

    public static class Animation {
        private final long duration;
        private final EasingFunction easing;
        private float startValue;
        private float targetValue;
        private float currentValue;
        private long startTime;
        private boolean completed;
        private Runnable onComplete;

        public Animation(float startValue, float targetValue, long duration, EasingFunction easing) {
            this.startValue = startValue;
            this.targetValue = targetValue;
            this.currentValue = startValue;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
            this.easing = easing;
            this.completed = false;
        }

        public float update() {
            if (completed) return currentValue;

            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / duration);

            if (progress >= 1.0f) {
                currentValue = targetValue;
                completed = true;
                if (onComplete != null) {
                    onComplete.run();
                }
            } else {
                float easedProgress = easing.apply(progress);
                currentValue = startValue + (targetValue - startValue) * easedProgress;
            }

            return currentValue;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setTarget(float newTarget) {
            this.startValue = currentValue;
            this.targetValue = newTarget;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
        }
    }

    public static class Easing {
        public static final EasingFunction LINEAR = t -> t;

        public static final EasingFunction EASE_IN_QUAD = t -> t * t;
        public static final EasingFunction EASE_OUT_QUAD = t -> 1 - (1 - t) * (1 - t);
        public static final EasingFunction EASE_IN_OUT_QUAD = t ->
                t < 0.5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);

        public static final EasingFunction EASE_IN_CUBIC = t -> t * t * t;
        public static final EasingFunction EASE_OUT_CUBIC = t -> 1 - (float) Math.pow(1 - t, 3);
        public static final EasingFunction EASE_IN_OUT_CUBIC = t ->
                t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;

        public static final EasingFunction EASE_IN_QUART = t -> t * t * t * t;
        public static final EasingFunction EASE_OUT_QUART = t -> 1 - (float) Math.pow(1 - t, 4);
        public static final EasingFunction EASE_IN_OUT_QUART = t ->
                t < 0.5f ? 8 * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 4) / 2;

        public static final EasingFunction EASE_IN_SINE = t -> 1 - (float) Math.cos(t * Math.PI / 2);
        public static final EasingFunction EASE_OUT_SINE = t -> (float) Math.sin(t * Math.PI / 2);
        public static final EasingFunction EASE_IN_OUT_SINE = t -> -((float) Math.cos(Math.PI * t) - 1) / 2;

        public static final EasingFunction EASE_IN_EXPO = t -> t == 0 ? 0 : (float) Math.pow(2, 10 * (t - 1));
        public static final EasingFunction EASE_OUT_EXPO = t -> t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
        public static final EasingFunction EASE_IN_OUT_EXPO = t -> {
            if (t == 0) return 0;
            if (t == 1) return 1;
            return t < 0.5f ? (float) Math.pow(2, 20 * t - 10) / 2 : (2 - (float) Math.pow(2, -20 * t + 10)) / 2;
        };

        public static final EasingFunction EASE_IN_CIRC = t -> 1 - (float) Math.sqrt(1 - t * t);
        public static final EasingFunction EASE_OUT_CIRC = t -> (float) Math.sqrt(1 - (t - 1) * (t - 1));
        public static final EasingFunction EASE_IN_OUT_CIRC = t ->
                t < 0.5f ? (1 - (float) Math.sqrt(1 - 4 * t * t)) / 2 : ((float) Math.sqrt(1 - (-2 * t + 2) * (-2 * t + 2)) + 1) / 2;

        public static final EasingFunction EASE_IN_BACK = t -> {
            float c1 = 1.70158f;
            float c3 = c1 + 1;
            return c3 * t * t * t - c1 * t * t;
        };

        public static final EasingFunction EASE_OUT_BACK = t -> {
            float c1 = 1.70158f;
            float c3 = c1 + 1;
            return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
        };

        public static final EasingFunction EASE_IN_OUT_BACK = t -> {
            float c1 = 1.70158f;
            float c2 = c1 * 1.525f;
            return t < 0.5f
                    ? ((float) Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                    : ((float) Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
        };

        public static final EasingFunction EASE_IN_ELASTIC = t -> {
            float c4 = (float) (2 * Math.PI) / 3;
            return t == 0 ? 0 : t == 1 ? 1 : -(float) Math.pow(2, 10 * t - 10) * (float) Math.sin((t * 10 - 10.75f) * c4);
        };

        public static final EasingFunction EASE_OUT_ELASTIC = t -> {
            float c4 = (float) (2 * Math.PI) / 3;
            return t == 0 ? 0 : t == 1 ? 1 : (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75f) * c4) + 1;
        };

        public static final EasingFunction EASE_IN_OUT_ELASTIC = t -> {
            float c5 = (float) (2 * Math.PI) / 4.5f;
            return t == 0 ? 0 : t == 1 ? 1 : t < 0.5f
                    ? -((float) Math.pow(2, 20 * t - 10) * (float) Math.sin((20 * t - 11.125f) * c5)) / 2
                    : ((float) Math.pow(2, -20 * t + 10) * (float) Math.sin((20 * t - 11.125f) * c5)) / 2 + 1;
        };

        public static final EasingFunction EASE_OUT_BOUNCE = t -> {
            float n1 = 7.5625f;
            float d1 = 2.75f;

            if (t < 1 / d1) {
                return n1 * t * t;
            } else if (t < 2 / d1) {
                return n1 * (t -= 1.5f / d1) * t + 0.75f;
            } else if (t < 2.5 / d1) {
                return n1 * (t -= 2.25f / d1) * t + 0.9375f;
            } else {
                return n1 * (t -= 2.625f / d1) * t + 0.984375f;
            }
        };

        public static final EasingFunction EASE_IN_BOUNCE = t -> 1 - EASE_OUT_BOUNCE.apply(1 - t);

        public static final EasingFunction EASE_IN_OUT_BOUNCE = t ->
                t < 0.5f ? (1 - EASE_OUT_BOUNCE.apply(1 - 2 * t)) / 2 : (1 + EASE_OUT_BOUNCE.apply(2 * t - 1)) / 2;
    }

    public static class AnimationSequence {
        private final String baseId;
        private final SequenceStep[] steps;
        private int currentStep;
        private long sequenceStartTime;
        private boolean completed;

        public AnimationSequence(String baseId, SequenceStep... steps) {
            this.baseId = baseId;
            this.steps = steps;
            this.currentStep = 0;
            this.sequenceStartTime = System.currentTimeMillis();
            this.completed = false;
        }

        public float update() {
            if (completed) return steps[steps.length - 1].targetValue;

            long elapsed = System.currentTimeMillis() - sequenceStartTime;
            long totalDelay = 0;

            for (int i = 0; i < currentStep; i++) {
                totalDelay += steps[i].delay + steps[i].duration;
            }

            if (elapsed < totalDelay + steps[currentStep].delay) {
                return currentStep == 0 ? steps[0].startValue : steps[currentStep - 1].targetValue;
            }

            String stepId = baseId + "_step_" + currentStep;
            float value = animate(stepId, steps[currentStep].targetValue,
                    steps[currentStep].duration, steps[currentStep].easing);

            if (isAnimationCompleted(stepId)) {
                currentStep++;
                if (currentStep >= steps.length) {
                    completed = true;
                }
            }

            return value;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void reset() {
            currentStep = 0;
            sequenceStartTime = System.currentTimeMillis();
            completed = false;

            for (int i = 0; i < steps.length; i++) {
                removeAnimation(baseId + "_step_" + i);
            }
        }
    }

    public record SequenceStep(float startValue, float targetValue, long duration, long delay, EasingFunction easing) {

        public SequenceStep(float startValue, float targetValue, long duration) {
                this(startValue, targetValue, duration, 0, Easing.EASE_OUT_CUBIC);
            }
        }
} 