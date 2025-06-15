

---

# OCS Project (Online Charging System)


**Description:**
A distributed, microservice-based Online Charging System (OCS) designed for real-time telecommunications billing. Built with Spring Boot, Kafka, Hazelcast, and Kubernetes for scalability, resilience, and high availability.

---

## Overview

The **OCS Project** (Online Charging System) is a distributed, scalable telecommunications solution that handles real-time charging for services like voice, SMS, and data. The architecture includes microservices, distributed caching, containerization, and automated deployment.

## Architecture

**Main Components:**

* **Diameter Gateway (DGW)** – Diameter message handling with Akka Cluster.
* **Charging Gateway Function (CGF)** – Business rule application and event processing.
* **Account Order Management (AOM)** – Manages user accounts and orders.
* **Account Balance Management Function (ABMF)** – Manages balances and top-ups.
* **Notification Framework (NF)** – Sends real-time notifications.
* **Traffic Generator Function (TGF)** – Generates synthetic traffic for testing and validation.
* **Mobile App** – End-user application.

**Microservices use:**

* **Spring Boot**
* **Kafka** & **Kafdrop**
* **Hazelcast Cluster**
* **Apache Ignite**
* **Docker & Kubernetes**

## 🗂 System Design

### Architectural Design

![Architectural Design](./Pictures/architecturaldesign.png)

### Database Design

![Database Design](./Pictures/dbdesign.png)

### Use Case Diagram

![Use Case](./Pictures/usecase.png)

## Modules and Their Diagrams

---

### **Diameter Gateway (DGW)**

![DGW](./Pictures/dgw.png)
**Function:** Receives network traffic and performs user authentication using Hazelcast. If the subscriber exists, forwards usage data to the OCS using Akka actors.

---

### **Charging Gateway Function (CGF)**

![CGF](./Pictures/cgf.png)
**Function:** Consumes general usage events from Kafka and logs detailed records into the database.

---

### **Account Order Management (AOM)**

![AOM](./Pictures/aom.png)
**Function:** Manages user and package balance information. Stores and manages data across three databases: Ignite, Hazelcast, and Oracle DB.

---

### **Account Balance Management Function (ABMF)**

![ABMF](./Pictures/abmf.png)
**Function:** Reads balance data from Kafka and synchronizes persistent records with Ignite database.

---

### **Notification Framework (NF)**

![NF](./Pictures/nf.png)
**Function:** Listens for Kafka triggers and sends user notifications for events like 80% usage or overages.

---

### **Traffic Generator Function (TGF)**

![TGF](./Pictures/tgf.png)
**Function:** Generates synthetic traffic and sends it to the DGW for system testing and validation.

---

---

## ☁️ AWS Deployment Architecture

![AWS Architecture](./Pictures/aws.png)

The OCS system is deployed on **Amazon Web Services (AWS)** using a secure and scalable infrastructure. The deployment is structured within a Virtual Private Cloud (VPC), separating public and private subnets for better isolation and control.

**Key Components:**

- EC2 instances for service hosting  
- Application Load Balancer (ALB) for routing traffic  
- NAT Gateway for secure outbound internet access  
- S3 for asset storage  
- IAM roles and security groups for controlled access  
- Multi-AZ setup for high availability

All cloud resources are provisioned automatically using Terraform.

---

## 🛠️ Infrastructure as Code – Terraform

![Terraform](./Pictures/terraform.png)

**Terraform** is used to define and provision the entire AWS infrastructure. It enables consistent, repeatable, and version-controlled deployments across environments.

**What Terraform Manages:**

- VPC, Subnets (Public/Private)  
- EC2 Instances, Elastic IPs, NAT Gateway  
- Route Tables, Internet Gateway  
- Load Balancer & Target Groups  
- IAM Roles & Policies  
- S3 Buckets and Security Groups

Terraform modules are structured for reusability and clarity. Infrastructure changes are tracked and deployed through declarative `.tf` files.

---

## ⚙️ Configuration Management – Ansible

![Ansible](./Pictures/ansible.png)

**Ansible** handles all post-provisioning server configuration. Once the infrastructure is ready, Ansible playbooks automate the installation and setup of necessary services across all nodes.

**Ansible Automates:**

- Installation of Java, Docker, Kubernetes CLI tools  
- Deployment of Jenkins, Nexus, SonarQube  
- Configuration of service-specific environment variables  
- Systemd service setups for custom modules  
- Role-based task execution with idempotent playbooks

This ensures consistent configuration, eliminates manual steps, and supports fully automated system provisioning.

---



### **Mobile App**

![Mobile App](./Pictures/mobileapp.png)
**Function:** Provides user interface for balance checks, top-ups, and usage monitoring.

---

## 🔄 CI/CD Pipeline

![CI/CD Pipeline](./Pictures/ci-cd.png)

*Describes the continuous integration and deployment workflow from development to production.*

---

## ⚙ Technologies

* **Java 17** / **Spring Boot**
* **Hazelcast / Ignite / Kafka**
* **Docker / Kubernetes**
* **Akka Cluster**
* **PostgreSQL / MongoDB** *(if applicable to DB design)*

## ✅ Development Principles

* Business logic isolated in **service layers**.
* Lightweight **controllers**.
* Follows **SOLID** and **clean architecture**.
* GOAL : Automated testing and containerized deployment.

## 🔍 Monitoring

* **Hazelcast Management Center**
* **Ignite Dashboard**
* **Kafdrop** for Kafka topic inspection.


## 📜 License

MIT License.

---

