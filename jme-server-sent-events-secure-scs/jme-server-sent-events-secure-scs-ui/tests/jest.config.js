"use strict";
globalThis.ngJest = {
  skipNgcc: true,
};
module.exports = {
  preset: "jest-preset-angular",
  setupFilesAfterEnv: ["<rootDir>/tests/setup-jest.ts"],
  coverageDirectory: "<rootDir>/coverage/sonarQube",
  collectCoverageFrom: ["<rootDir>/src/**/*.ts"],
  testResultsProcessor: "jest-sonar-reporter",
  globalSetup: "<rootDir>/tests/global-setup-jest.js",
  roots: ["src"],
  // Allow Jest to transform ESM packages that are not pre-compiled to CJS.
  // By default Jest ignores all of node_modules; we carve out the ESM-only
  // packages so that the Angular compiler (via jest-preset-angular) can handle them.
  transformIgnorePatterns: [
    "node_modules/(?!(@angular|@quadrel-enterprise-ui|@quadrel-ui|@ngrx|@oblique|ngx-translate-multi-http-loader|@ngx-translate|ngx-editor|angular-auth-oidc-client)/)"
  ],
  prettierPath: null, // prettier 3 does not yet work with jest and inline snapshots
};
