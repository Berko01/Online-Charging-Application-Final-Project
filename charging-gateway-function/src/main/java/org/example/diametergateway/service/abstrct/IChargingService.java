package org.example.charginggatewayfunction.service.abstrct;

import com.ramobeko.kafka.message.CGFKafkaMessage;

public interface IChargingService {
    // Örneğin, CGFKafkaMessage'ı alıp veritabanına kaydeden bir metot
    void processCGFMessage(CGFKafkaMessage message);
}
