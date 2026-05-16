# UML Models for Cardiovascular Health Monitoring System

This folder contains UML class diagrams and explanations for four key subsystems of the Cardiovascular Health Monitoring
System (CHMS). The models focus on modularity, clear responsibilities, access control, and extensibility.

---

## 1. Alert Generation System

```mermaid
classDiagram
    class AlertGenerator {
        -DataStorage dataStorage
        -PatientIdentifier patientIdentifier
        -ThresholdManager thresholdManager
        -AlertManager alertManager
        +evaluateData(patientId: int, data: PatientData): void
        -isCritical(data: PatientData, threshold: ThresholdRule): boolean
    }

    class Alert {
        -int patientId
        -String condition
        -long timestamp
        -String severity
        +getPatientId(): int
        +getCondition(): String
        +getTimestamp(): long
        +getSeverity(): String
    }

    class AlertManager {
        -List~MedicalStaff~ staffMembers
        +dispatchAlert(alert: Alert): void
        +registerStaff(staff: MedicalStaff): void
    }

    class ThresholdManager {
        -Map~Integer, List~ThresholdRule~~ patientThresholds
        +getRulesForPatient(patientId: int): List~ThresholdRule~
        +addRule(patientId: int, rule: ThresholdRule): void
        +removeRule(patientId: int, rule: ThresholdRule): void
    }

    class ThresholdRule {
        -String vitalType
        -double minValue
        -double maxValue
        -String severity
        +isViolated(data: PatientData): boolean
    }

    class PatientData {
        -int patientId
        -String vitalType
        -double value
        -long timestamp
        +getPatientId(): int
        +getVitalType(): String
        +getValue(): double
        +getTimestamp(): long
    }

    class DataStorage {
        +storeData(data: PatientData): void
        +getRecentData(patientId: int): List~PatientData~
    }

    class PatientIdentifier {
        +validatePatientId(patientId: int): boolean
        +matchPatient(patientId: int): HospitalPatient
    }

    class MedicalStaff {
        -int staffId
        -String name
        -String role
        +receiveAlert(alert: Alert): void
    }

    AlertGenerator --> DataStorage : stores and retrieves data
    AlertGenerator --> PatientIdentifier : validates patient
    AlertGenerator --> ThresholdManager : gets patient rules
    AlertGenerator --> AlertManager : sends alert
    AlertManager "1" o-- "0..*" MedicalStaff : routes to
    ThresholdManager "1" o-- "0..*" ThresholdRule : manages
    AlertGenerator ..> Alert : creates
    AlertGenerator ..> PatientData : evaluates
    ThresholdRule ..> PatientData : checks
```

### Explanation

The Alert Generation System is responsible for evaluating incoming patient data in real time and deciding whether an
alert should be created. `AlertGenerator` is the central coordinator, but it does not do every task itself. It uses
`PatientIdentifier` to confirm that the incoming patient ID matches a known hospital patient, `DataStorage` to store or
retrieve recent patient data, `ThresholdManager` to obtain personalized threshold rules, and `AlertManager` to route
alerts to medical staff.

The design separates responsibilities clearly. `ThresholdRule` represents one medical rule, such as heart rate being
above a maximum value. This allows different patients to have different thresholds instead of using one global rule for
everyone. `Alert` is a simple data object that stores the patient ID, condition, timestamp, and severity. `AlertManager`
handles dispatching alerts, so the generator does not need to know which staff member receives which alert.

Most attributes are private to protect patient-related data and prevent uncontrolled access. Public methods expose only
the operations that other parts of the system need. The relationship between `ThresholdManager` and `ThresholdRule` is
aggregation because the manager keeps a collection of reusable rules. The relationship between `AlertManager` and
`MedicalStaff` is also aggregation because staff members exist independently from the alert manager. This structure
keeps the subsystem modular, easier to extend, and safer for a hospital monitoring context.

## 2. Data Storage System

```mermaid
classDiagram
    class DataStorage {
        -Map~Integer, List~PatientData~~ patientRecords
        -AccessController accessController
        -DeletionPolicy deletionPolicy
        +storeData(data: PatientData): void
        +getData(patientId: int, requester: MedicalStaff): List~PatientData~
        +deleteExpiredData(): void
    }

    class PatientData {
        -int patientId
        -String vitalType
        -double value
        -long timestamp
        -int version
        +getPatientId(): int
        +getVitalType(): String
        +getValue(): double
        +getTimestamp(): long
        +getVersion(): int
    }

    class DataRetriever {
        -DataStorage dataStorage
        +requestPatientData(patientId: int, requester: MedicalStaff): List~PatientData~
        +requestDataByTimeRange(patientId: int, startTime: long, endTime: long): List~PatientData~
    }

    class AccessController {
        -Map~String, List~String~~ rolePermissions
        +canAccess(requester: MedicalStaff, patientId: int): boolean
        +canDelete(requester: MedicalStaff): boolean
    }

    class DeletionPolicy {
        -int retentionDays
        +isExpired(data: PatientData): boolean
        +getRetentionDays(): int
    }

    class MedicalStaff {
        -int staffId
        -String name
        -String role
        +getRole(): String
        +getStaffId(): int
    }

    class AuditLog {
        -List~String~ entries
        +logAccess(staffId: int, patientId: int, timestamp: long): void
        +logDeletion(patientId: int, timestamp: long): void
    }

    DataStorage "1" *-- "0..*" PatientData : stores
    DataStorage --> AccessController : checks permission
    DataStorage --> DeletionPolicy : applies
    DataStorage --> AuditLog : records actions
    DataRetriever --> DataStorage : queries
    DataRetriever --> MedicalStaff : receives request from
    AccessController --> MedicalStaff : verifies role
```

### Explanation

The Data Storage System securely stores incoming patient measurements in an organized way. `DataStorage` groups records
by patient ID, making it easy to pull up everything you need. Each `PatientData` object captures one vital sign reading:
patient ID, measurement type, value, timestamp, and version number.
The timestamp lets you pull historical data whenever needed. The version number creates a trail of changes—if a record
gets updated or replaced, you can see what happened and when. In healthcare, that audit trail is essential.

The design separates storage from retrieval. `DataRetriever` handles requests from medical staff and asks `DataStorage`
for the needed records. This prevents external users from accessing the stored data directly. Before patient data is
returned, `AccessController` checks whether the requesting staff member has permission to view the data. This is
important because cardiovascular data is sensitive medical information and should only be available to authorized roles.

`DeletionPolicy` represents the rule for removing old records after a defined retention period. This keeps the system
from storing unnecessary data forever and supports privacy requirements. `AuditLog` records access and deletion actions,
making the system more traceable and accountable. The relationship between `DataStorage` and `PatientData` is
composition because records are stored as part of the storage structure. The rest of the relationships are associations
because the classes collaborate while keeping separate responsibilities.

## 3. Patient Identification System

```mermaid
classDiagram
    class PatientIdentifier {
        -HospitalPatientRepository patientRepository
        -IdentityManager identityManager
        +matchPatient(simulatorPatientId: int): HospitalPatient
        +validateIncomingId(simulatorPatientId: int): boolean
    }

    class HospitalPatient {
        -int hospitalPatientId
        -int simulatorPatientId
        -String anonymizedName
        -String medicalHistoryReference
        +getHospitalPatientId(): int
        +getSimulatorPatientId(): int
        +getMedicalHistoryReference(): String
    }

    class IdentityManager {
        -List~IdentityMismatch~ mismatches
        +verifyMatch(simulatorPatientId: int, patient: HospitalPatient): boolean
        +handleMismatch(mismatch: IdentityMismatch): void
        +getUnresolvedMismatches(): List~IdentityMismatch~
    }

    class HospitalPatientRepository {
        -Map~Integer, HospitalPatient~ patientIndex
        +findBySimulatorId(simulatorPatientId: int): HospitalPatient
        +addPatient(patient: HospitalPatient): void
        +removePatient(simulatorPatientId: int): void
    }

    class IdentityMismatch {
        -int simulatorPatientId
        -String reason
        -long timestamp
        -String status
        +markResolved(): void
        +getReason(): String
        +getStatus(): String
    }

    class PatientData {
        -int patientId
        -String vitalType
        -double value
        -long timestamp
        +getPatientId(): int
        +getVitalType(): String
        +getValue(): double
    }

    class AuditLog {
        -List~String~ entries
        +logMatch(patientId: int, timestamp: long): void
        +logMismatch(patientId: int, reason: String): void
    }

    class MedicalStaff {
        -int staffId
        -String name
        -String role
        +reviewMismatch(mismatch: IdentityMismatch): void
    }

    PatientIdentifier --> HospitalPatientRepository : searches in
    PatientIdentifier --> IdentityManager : asks to verify
    PatientIdentifier ..> PatientData : reads patient ID from
    HospitalPatientRepository "1" o-- "0..*" HospitalPatient : stores
    IdentityManager "1" o-- "0..*" IdentityMismatch : manages
    IdentityManager --> AuditLog : logs identity events
    MedicalStaff --> IdentityMismatch : reviews
    PatientIdentifier --> HospitalPatient : returns matched patient
```

### Explanation

The Patient Identification System links incoming simulator data to the correct hospital patient record.
`PatientIdentifier` is the main entry point. It receives the simulator patient ID from incoming `PatientData` and asks
`HospitalPatientRepository` to find the matching `HospitalPatient`. This keeps the matching logic separate from the rest
of the monitoring system, so alert generation and data storage do not need to know how hospital identities are resolved.

`HospitalPatient` contains the hospital-side patient information, including the hospital patient ID, simulator patient
ID, anonymized name, and a reference to medical history. Sensitive information is kept private, and the model uses an
anonymized name instead of exposing full personal details. `HospitalPatientRepository` stores and retrieves patient
records, while `IdentityManager` checks whether the match is valid and handles edge cases.

If no valid match is found, or if the incoming ID looks suspicious, an `IdentityMismatch` object is created. This
records the simulator ID, reason, timestamp, and status of the mismatch. `MedicalStaff` can review unresolved
mismatches, while `AuditLog` records matching and mismatch events for traceability. This is important in a hospital
system because incorrect patient identification can lead to unsafe alerts, wrong medical decisions, or privacy
violations. The design separates matching, storage, mismatch handling, and review responsibilities, which makes the
subsystem safer and easier to maintain.

## 4. Data Access Layer

```mermaid
classDiagram
    class DataListener {
        <<interface>>
        +startListening(): void
        +stopListening(): void
        +receiveRawData(): String
    }

    class TCPDataListener {
        -int port
        -boolean active
        +startListening(): void
        +stopListening(): void
        +receiveRawData(): String
    }

    class WebSocketDataListener {
        -int port
        -boolean active
        +startListening(): void
        +stopListening(): void
        +receiveRawData(): String
    }

    class FileDataListener {
        -String filePath
        -boolean active
        +startListening(): void
        +stopListening(): void
        +receiveRawData(): String
    }

    class DataParser {
        +parse(rawData: String): PatientData
        -validateFormat(rawData: String): boolean
    }

    class DataSourceAdapter {
        -DataListener listener
        -DataParser parser
        -DataStorage dataStorage
        +collectData(): void
        +sendToStorage(data: PatientData): void
    }

    class PatientData {
        -int patientId
        -String vitalType
        -double value
        -long timestamp
        +getPatientId(): int
        +getVitalType(): String
        +getValue(): double
        +getTimestamp(): long
    }

    class DataStorage {
        +storeData(data: PatientData): void
        +getRecentData(patientId: int): List~PatientData~
    }

    class InvalidDataException {
        -String message
        +getMessage(): String
    }

    DataListener <|.. TCPDataListener
    DataListener <|.. WebSocketDataListener
    DataListener <|.. FileDataListener
    DataSourceAdapter "1" --> "1" DataListener : reads from
    DataSourceAdapter "1" --> "1" DataParser : parses with
    DataSourceAdapter "1" --> "1" DataStorage : stores into
    DataParser ..> PatientData : creates
    DataParser ..> InvalidDataException : throws
    DataStorage "1" o-- "0..*" PatientData : stores
```

### Explanation

The Data Access Layer connects the external signal generator to the internal CHMS system. The signal generator can send
data through different sources, such as TCP, WebSocket, or file logs, so the design uses a shared `DataListener`
interface. `TCPDataListener`, `WebSocketDataListener`, and `FileDataListener` all implement the same operations:
starting the connection, stopping it, and receiving raw data. This means the rest of the system does not need to know
where the data came from.

`DataSourceAdapter` coordinates the flow between the listener, parser, and storage. It receives raw input from a
selected `DataListener`, sends that raw input to `DataParser`, and then passes the resulting `PatientData` object to
`DataStorage`. This keeps networking, file reading, parsing, and storage separated into different responsibilities.

`DataParser` standardizes incoming data into a common `PatientData` format. If the input is malformed or missing
required fields, it can raise an `InvalidDataException`. This prevents broken or incomplete data from entering the
monitoring system. `PatientData` keeps only the structured information that the rest of the system needs: patient ID,
vital type, value, and timestamp.

This design is built to grow. If you need to pull data from a new source down the road—say, a REST API or a message
queue you can add a listener for it without touching the storage or alert systems. That clean separation means the
system stays maintainable and crucially, stays reliable in a real-time healthcare setting where mistakes are costly.