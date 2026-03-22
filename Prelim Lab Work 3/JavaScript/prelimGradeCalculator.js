function compute() {
    const a = Number(document.getElementById("attendance").value);
    const l1 = Number(document.getElementById("lab1").value);
    const l2 = Number(document.getElementById("lab2").value);
    const l3 = Number(document.getElementById("lab3").value);

    const labAvg = (l1 + l2 + l3) / 3;
    const classStanding = (a * 0.40) + (labAvg * 0.60);

    const pass = (75 - (classStanding * 0.30)) / 0.70;
    const excellent = (100 - (classStanding * 0.30)) / 0.70;

    let result =
        `Lab Work Average: ${labAvg.toFixed(2)}\n` +
        `Class Standing: ${classStanding.toFixed(2)}\n\n`;

    result += pass <= 100
        ? `Required Exam to PASS: ${pass.toFixed(2)}\n`
        : `Passing is not achievable.\n`;

    result += excellent <= 100
        ? `Required Exam for EXCELLENT: ${excellent.toFixed(2)}`
        : `Excellent standing is not achievable.`;

    document.getElementById("output").textContent = result;
}
