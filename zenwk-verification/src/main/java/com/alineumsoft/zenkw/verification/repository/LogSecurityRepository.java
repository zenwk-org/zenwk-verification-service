package com.alineumsoft.zenkw.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alineumsoft.zenkw.verification.entity.LogSecurity;

/**
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 */
public interface LogSecurityRepository extends JpaRepository<LogSecurity, Long> {

}
