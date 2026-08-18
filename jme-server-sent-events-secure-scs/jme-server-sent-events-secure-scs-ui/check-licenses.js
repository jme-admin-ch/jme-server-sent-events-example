/**
 * Checks the licenses of all npm dependencies against the policy in license-check-config.json.
 *
 * A dependency passes if its SPDX license expression is covered by "allowedLicenses" (OR alternatives
 * only need one accepted license, AND terms need all of them), or if the package is listed in
 * "exceptions". Exception keys are "<package>@<version>", where the version may be "*" to accept any
 * version - including for scoped packages such as "@scope/package@*". Using the wildcard keeps
 * dependency updates (e.g. Renovate) from breaking the build whenever an exempted package is bumped.
 */
const fs = require("fs");
const path = require("path");
const checker = require("license-checker");

const config = JSON.parse(fs.readFileSync(path.join(__dirname, "license-check-config.json"), "utf8"));
const allowedLicenses = config.allowedLicenses || [];
const exceptions = config.exceptions || {};

/**
 * Splits "@scope/package@1.2.3" into ["@scope/package", "1.2.3"].
 */
function splitPackageKey(key) {
  const separatorIndex = key.lastIndexOf("@");
  return separatorIndex > 0 ? [key.slice(0, separatorIndex), key.slice(separatorIndex + 1)] : [key, ""];
}

function findException(key) {
  const [name] = splitPackageKey(key);
  return exceptions[key] || exceptions[`${name}@*`];
}

/**
 * Evaluates a (possibly compound) SPDX expression such as "(MIT OR CC0-1.0)" or "(MIT AND CC-BY-3.0)".
 */
function isAllowedExpression(expression) {
  const term = expression
    .trim()
    .replace(/^\((.*)\)$/, "$1")
    .trim();
  if (term.includes(" OR ")) {
    return term.split(" OR ").some(isAllowedExpression);
  }
  if (term.includes(" AND ")) {
    return term.split(" AND ").every(isAllowedExpression);
  }
  return allowedLicenses.includes(term);
}

function isAllowedLicense(licenses) {
  if (!licenses) {
    return false;
  }
  // license-checker reports the legacy "licenses" array form of package.json as an array, meaning dual licensing.
  return Array.isArray(licenses) ? licenses.some(isAllowedExpression) : isAllowedExpression(licenses);
}

checker.init({ start: __dirname }, (error, packages) => {
  if (error) {
    console.error("Failed to collect the dependency licenses:", error);
    process.exit(1);
  }

  const licenseCounts = {};
  const usedExceptions = {};
  const problems = [];
  let allowedCount = 0;

  Object.keys(packages).forEach((key) => {
    const dependency = packages[key];
    const licenses = `${dependency.licenses}`;
    licenseCounts[licenses] = (licenseCounts[licenses] || 0) + 1;

    if (isAllowedLicense(dependency.licenses)) {
      allowedCount++;
      return;
    }
    const exception = findException(key);
    if (exception) {
      usedExceptions[key] = exception;
      return;
    }
    problems.push({ key, ...dependency });
  });

  console.log("Dependency licenses in use:");
  Object.keys(licenseCounts)
    .sort()
    .forEach((license) => console.log(`  ${license} (${licenseCounts[license]})`));

  if (Object.keys(usedExceptions).length) {
    console.log("\nAccepted license exceptions:");
    Object.keys(usedExceptions)
      .sort()
      .forEach((key) => console.log(`  ${key}\n    Reason: ${usedExceptions[key].reason || "No reason given"}`));
  }

  if (problems.length) {
    console.log("\nProblems with the licenses of these dependencies:");
    problems.forEach((problem) =>
      console.log(
        `  ${problem.key}\n` +
          `    License:     ${problem.licenses}\n` +
          `    Repository:  ${problem.repository}\n` +
          `    Publisher:   ${problem.publisher}\n` +
          `    Url:         ${problem.url}\n`,
      ),
    );
  }

  const summary = `Allowed (${allowedCount}) Exceptions (${Object.keys(usedExceptions).length}) Problems (${problems.length})`;
  if (problems.length) {
    console.log(`\nLicenses not ok: ${summary}`);
    process.exit(1);
  }
  console.log(`\nAll licenses ok: ${summary}`);
});
