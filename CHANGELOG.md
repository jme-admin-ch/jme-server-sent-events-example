# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [6.0.0] - 2026-08-20

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.5.0 → 40.0.0 (major)

## [5.1.0] - 2026-08-19

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.3.0 → 39.5.0 (minor)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 9.0.0 → 9.1.0 (minor)
- **@jeap/jeap-frontend-license-checker**: 1.0.1 → 1.0.2 (patch)

## [5.0.0] - 2026-08-19

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.0.1 → 39.3.0 (minor)
- **uuid**: 11.1.1 → 14.0.2 (major)

### Changed
- Both UI modules now let Jest transform `uuid`, which ships as ESM only since v13 and can no longer be loaded untransformed by Jest's CommonJS runtime. The Jest configuration of the two modules is now shared via `jest.config.base.js` so it cannot drift apart.

## [4.0.1] - 2026-08-18

### Changed
- Replaced the frontend license checker with `@jeap/jeap-frontend-license-checker`, and the license check now runs as part of the npm frontend build.

### Added
- Both UI modules now generate a `THIRD-PARTY-FRONTEND-LICENSES.md` and carry the full license texts of their redistributed dependencies as files under `third-party-licenses/`.

## [4.0.0] - 2026-08-18

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 39.0.0 → 39.0.1 (patch)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 8.6.0 → 9.0.0 (major)
- **@quadrel-enterprise-ui/framework**: 20.34.0 → 20.35.0 (minor)

## [3.0.1] - 2026-08-18

### Security
- **deepmerge-ts**: 7.1.5 → 8.0.1 (CVE-2026-40345, npm override)
- **uuid**: 9.0.1 → 11.1.1 (CVE-2026-41907, npm override)

## [3.0.0] - 2026-08-13

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 38.1.0 → 39.0.0 (major)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 8.2.0 → 8.6.0 (minor)
- **@typescript-eslint/parser**: 8.66.0 → 8.67.0 (minor)
- **@typescript-eslint/eslint-plugin**: 8.66.0 → 8.67.0 (minor)
- **@quadrel-enterprise-ui/framework**: 20.32.2 → 20.34.0 (minor)

## [2.0.1] - 2026-08-07

### Dependencies
- **eslint**: 10.8.0 → 10.8.1 (patch)

## [2.0.0] - 2026-08-06

### Dependencies
- **ch.admin.bit.jeap:jeap-spring-boot-parent**: 38.0.1 → 38.1.0 (minor)
- **ch.admin.bit.jeap:jeap-oauth-mock-server**: 8.0.1 → 8.2.0 (minor)
- **zone.js**: 0.15.1 → 0.16.2 (minor)
- **prettier**: 3.8.1 → 3.9.6 (minor)
- **jest-preset-angular**: 14.6.2 → 17.0.0 (major)
- **jest-environment-jsdom**: 29.7.0 → 30.4.1 (major)
- **jest**: 29.7.0 → 30.4.2 (major)
- **eslint-config-prettier**: 9.1.2 → 10.1.8 (major)
- **eslint**: 9.39.5 → 10.8.0 (major)
- **@typescript-eslint/parser**: 8.64.0 → 8.66.0 (minor)
- **@typescript-eslint/eslint-plugin**: 8.64.0 → 8.66.0 (minor)
- **@types/node**: 25.5.0 → 25.9.5 (minor)
- **@types/jest**: 29.5.14 → 30.0.0 (major)
- **@quadrel-enterprise-ui/framework**: 20.28.1 → 20.32.2 (minor)
- **@ngrx/store**: 20.1.0 → 21.1.1 (major)

## [1.0.2] - 2026-08-05

### Fixed

- Generate each sources and javadoc artifact once so immutable repository deployment completes successfully.

## [1.0.1] - 2026-08-04

### Fixed

- Published collision-free portable artifacts so consumers do not resolve legacy executable JARs from internal caches.

## [1.0.0] - 2026-08-03

### Added

- Initial open-source release with anonymous and authenticated SSE examples.
- Local OAuth2 mock server, PostgreSQL/Kafka Docker Compose infrastructure, and browser integration tests.
