# orielair-api
🌬️ Core Essence
Oriel Air is a smart asthma exacerbation prediction system By leveraging high-fidelity physiological data from wearables and advanced machine learning,
the system provides proactive early warnings to help users manage asthma before symptoms become critica

🚀 Key Features
Dual-Window Prediction: Specialized models for two time scales:
Short-term (1h): Detects acute physiological "crashes" and immediate risks.
Long-term (24h): Forecasts gradual deterioration based on 24-hour trends.
State-Aware Vitals: Distinguishes between awake and sleep states to filter noise and capture clean nocturnal baselines (HRV, RR, SpO2).
Cardiorespiratory Coupling: Analyzes the synchronization between heart rate and breathing to detect autonomic distress.
Event-Driven Architecture: Built on a Kappa/EDA pipeline for clinical-grade traceability using correlation_id and source_event_id.
🛠️ Technical Stack
Language: Kotlin
Framework: Spring Boot
AI Engine: H2O-3 (GBM, XGBoost, Random Forest)
Architecture: Feature-based structure with CQRS and URL-path versioning.
IoT Integration: Samsung Health Sensor SDK (Wear OS) and Withings Public API.

📂 API Structure
/v1/vitals: Ingestion of raw physiological signals.
/v1/predictions: Access to the "Oriel Score" and AI insights.
/v1/timeline: A synthesized narrative of the patient's respiratory story.
/v1/alerts: Real-time notifications for risk escalation.
🛡️ Robustness & Safety
As detailed in our checklists, the system implements a Quorum Gatekeeper to ensure no predictions are made on "garbage" data. We prioritize Recall in our models—ensuring we would rather trigger a false alarm than miss a real asthma flare-up.

