package com.curelink.test.dattri.config;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.curelink.test.dattri.entity.Protocol;
import com.curelink.test.dattri.repository.ProtocolRepository;

/**
 * Seeds default health protocols and policies into the {@code protocol} table on startup
 * if the table is empty. Safe to re-run: skips if data already exists.
 *
 * These protocols are matched against user queries at runtime and injected as
 * context into the LLM prompt.
 */
@Component
public class ProtocolSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProtocolSeeder.class);

    private final ProtocolRepository protocolRepository;

    public ProtocolSeeder(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (protocolRepository.count() > 0) {
            log.info("Protocols already seeded ({} rows). Skipping.", protocolRepository.count());
            return;
        }

        List<Protocol> protocols = List.of(
            new Protocol(
                UUID.randomUUID().toString(),
                "FEVER",
                "Fever Protocol",
                """
                For mild fever (< 38.5°C / 101.3°F):
                - Rest and stay hydrated (water, ORS, coconut water).
                - Paracetamol (500 mg) every 6 hours if uncomfortable.
                - Light clothing, cool room temperature.
                - Monitor every 4 hours.
                Escalate to a doctor if:
                - Fever > 39°C / 102.2°F.
                - Persists more than 3 days.
                - Accompanied by rash, breathlessness, severe headache, or convulsions.
                - Child under 3 months with any fever.
                """,
                Instant.now()
            ),
            new Protocol(
                UUID.randomUUID().toString(),
                "STOMACH_ACHE",
                "Stomach Ache Protocol",
                """
                For mild stomach pain or cramps:
                - Encourage light diet (khichdi, toast, bananas, rice).
                - Avoid spicy, oily, or dairy-heavy food.
                - Stay hydrated; ORS if loose stools present.
                - Antacid (e.g. Gelusil) for acidity-related pain.
                Escalate to a doctor if:
                - Pain is severe, sharp, or radiating.
                - Accompanied by high fever, vomiting blood, or black stools.
                - Persists more than 24 hours or is worsening.
                - Located in lower-right abdomen (possible appendicitis).
                """,
                Instant.now()
            ),
            new Protocol(
                UUID.randomUUID().toString(),
                "HEADACHE",
                "Headache Protocol",
                """
                For tension or mild headache:
                - Rest in a quiet, dark room.
                - Stay hydrated; dehydration is a common trigger.
                - Paracetamol (500 mg) or Ibuprofen (400 mg) if needed.
                - Cold or warm compress on forehead/neck.
                - Avoid screens for 30–60 minutes.
                Escalate to a doctor if:
                - Sudden, severe "thunderclap" headache.
                - Accompanied by vision changes, confusion, or weakness.
                - Headache after head injury.
                - Persists more than 2 days or keeps recurring.
                """,
                Instant.now()
            ),
            new Protocol(
                UUID.randomUUID().toString(),
                "COLD_COUGH",
                "Cold & Cough Protocol",
                """
                For common cold and mild cough:
                - Rest and stay well-hydrated (warm fluids, honey-ginger tea).
                - Steam inhalation for nasal congestion.
                - Saline nasal drops for blocked nose.
                - Lozenges or warm salt-water gargle for sore throat.
                Escalate to a doctor if:
                - Cough persists more than 2 weeks.
                - Coughing up blood or yellow/green mucus.
                - Breathlessness or chest pain.
                - High fever (> 39°C) alongside cough.
                """,
                Instant.now()
            ),
            new Protocol(
                UUID.randomUUID().toString(),
                "DIABETES",
                "Diabetes Management Protocol",
                """
                General guidance for diabetic users:
                - Maintain consistent meal timings; avoid skipping meals.
                - Limit refined carbohydrates, sugary drinks, and processed food.
                - Regular light exercise (30 min walk daily recommended).
                - Monitor blood glucose as advised by their doctor.
                - Keep emergency sugar source (glucose tablet, juice) handy for hypoglycemia.
                Signs of hypoglycemia (low sugar): shakiness, sweating, confusion, dizziness.
                Action: consume 15g fast-acting sugar immediately (3-4 glucose tablets or half a cup of juice).
                Escalate to a doctor if:
                - Blood sugar is consistently out of target range.
                - Signs of hypoglycemia do not resolve in 15 minutes.
                - Any new symptoms like chest pain, vision changes, or foot wounds.
                """,
                Instant.now()
            ),
            new Protocol(
                UUID.randomUUID().toString(),
                "REFUND_POLICY",
                "Refund Policy",
                """
                Curelink refund and cancellation policy:
                - Subscriptions can be cancelled within 48 hours of purchase for a full refund.
                - After 48 hours, subscriptions are non-refundable but remain active until the end of the billing period.
                - One-time consultations can be rescheduled up to 2 hours before the appointment.
                - Refunds are processed within 5–7 business days to the original payment method.
                - To request a refund, the user should contact support at support@cure.link or via the app.
                """,
                Instant.now()
            )
        );

        protocolRepository.saveAll(protocols);
        log.info("Seeded {} protocols successfully.", protocols.size());
    }
}
